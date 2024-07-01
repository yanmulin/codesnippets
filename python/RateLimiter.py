from abc import ABC, abstractmethod
from datetime import timedelta
import redis
import uuid
import time
import textwrap
import unittest


'''
start a Redis docker container: 
  docker run --rm --name redis redis:latest
'''
REDIS = redis.Redis(decode_responses=True)


class RateLimiter(ABC):
    @abstractmethod
    def allow(self, key): ...

    def _get_timestamp(self):
        return int(time.time())


@RateLimiter.register
class RedisCounterRateLimiter(RateLimiter):
    DEFAULT_PREFIX = 'ratelimit.counter'
    DEFAULT_LIMIT = 5
    DEFAULT_EXPIRE = timedelta(seconds=3)

    def __init__(self, limit=DEFAULT_LIMIT, expire=DEFAULT_EXPIRE, prefix=DEFAULT_PREFIX):
        self.limit = limit
        self.expire = expire
        self.prefix = prefix

    '''
    cache key: 
    - <prefix>.<key>.<ts>.counter

    Redis commands:
    MULTI INCR(key); EXPIRE(key, 10); EXEC
    '''
    def allow(self, key):
        cache_key = self._get_cache_key(key, self._get_timestamp())
        p = REDIS.pipeline()
        p.incr(cache_key).expire(cache_key, self.expire)
        count, _ = p.execute()
        return count - 1 < self.limit

    def _get_cache_key(self, key, ts):
        return '%s.%s.%s.counter' % (self.prefix, key, ts)


@RateLimiter.register
class RedisTokensBucketRateLimiter(RateLimiter):
    DEFAULT_PREFIX = 'ratelimit.bucket'
    DEFAULT_RATE = 2
    DEFAULT_CAPACITY = 10
    LUA_SCRIPT = textwrap.dedent('''
        local tokens_key = KEYS[1]
        local timestamp_key = KEYS[2]

        local now = tonumber(ARGV[1])
        local rate = tonumber(ARGV[2])
        local capacity = tonumber(ARGV[3])
        local hits = tonumber(ARGV[4])

        local last_tokens = tonumber(redis.call("GET", tokens_key))
        if last_tokens == nil then
            last_tokens = capacity
        end
        local last_timestamp = tonumber(redis.call("GET", timestamp_key))
        if last_timestamp == nil then
            last_timestamp = 0
        end

        local delta = math.max(0, now - last_timestamp)
        local refilled_bucket_size = math.min(capacity, last_tokens + delta * rate)
        local bucket_size = refilled_bucket_size
        local allowed = refilled_bucket_size >= hits
        if allowed then
            bucket_size = refilled_bucket_size - hits
        end

        local ttl = math.floor(capacity / rate * 2)
        redis.call("SETEX", tokens_key, ttl, bucket_size)
        redis.call("SETEX", timestamp_key, ttl, now)

        return allowed
    ''')

    def __init__(self, rate=DEFAULT_RATE, capacity=DEFAULT_CAPACITY, prefix=DEFAULT_PREFIX):
        self.rate = rate
        self.capacity = capacity
        self.prefix = prefix

    '''
    atomic read/write with a Lua script
    cache keys: (both expired after X seconds)
    - <prefix>.<key>.tokens
    - <prefix>.<key>.timestamp 
    '''
    def allow(self, key):
        tokens_key, timestamp_key = self._get_cache_keys(key)
        return bool(REDIS.eval(self.LUA_SCRIPT, 2, tokens_key, timestamp_key, 
            self._get_timestamp(), self.rate, self.capacity, 1))

    def _get_cache_keys(self, key):
        tokens_key = '%s.%s.tokens' % (self.prefix, key)
        timestamp_key = '%s.%s.timestamp' % (self.prefix, key)
        return (tokens_key, timestamp_key)


@RateLimiter.register
class RedisConcurrentRequestsLimiter(RateLimiter):
    DEFAULT_LIMIT = 5
    DEFAULT_TTL = timedelta(seconds=60)
    DEFAULT_PREFIX = 'ratelimit.concurrent'
    LUA_SCRIPT = textwrap.dedent('''
        local key = KEYS[1]

        local now = tonumber(ARGV[1])
        local ttl = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        local request = ARGV[4]

        redis.call("ZREMRANGEBYSCORE", key, "-inf", now - ttl)
        local count = tonumber(redis.call("ZCARD", key)) or 0
        local allowed = count < limit
        if allowed then
            redis.call("ZADD", key, now, request)
            redis.call("EXPIRE", key, ttl)
        end

        return allowed
    ''')

    def __init__(self, limit = DEFAULT_LIMIT, ttl = DEFAULT_TTL, prefix = DEFAULT_PREFIX):
        self.limit = limit
        self.ttl = ttl
        self.prefix = prefix

    '''
    atomic read/write with a Lua script
    cache key: (sorted set)
    - <prefix>.<key>: <request id> with <timestamp> as score
    '''
    def allow(self, key, request_id):
        ts = self._get_timestamp()
        cache_key = self._get_cache_key(key)
        allowed = bool(REDIS.eval(self.LUA_SCRIPT, 1, cache_key, 
                                  ts, int(self.ttl.total_seconds()), self.limit, request_id))
        return allowed

    def after_request(self, key, request_id):
        ts = self._get_timestamp()
        cache_key = self._get_cache_key(key)
        REDIS.pipeline()\
            .zremrangebyscore(cache_key, '-inf', ts - self.ttl.total_seconds())\
            .zrem(cache_key, request_id)\
            .execute()

    def _get_cache_key(self, key):
        return '%s.%s' % (self.prefix, key)


'''
Notes
- no more than N request in X seconds
- accurate limit: no spike traffic at the edge of window
- minimum Y seconds between requests
- rejected requests are counted
'''
@RateLimiter.register
class RedisRollingWindowRateLimiter(RateLimiter):
    DEFAULT_PREFIX = 'ratelimit.rolling'
    DEFAULT_LIMIT = 5
    DEFAULT_WINDOW_SIZE = timedelta(seconds=5)
    DEFAULT_MIN_GAP = timedelta(seconds=1)

    def __init__(self, limit = DEFAULT_LIMIT, window = DEFAULT_WINDOW_SIZE, 
                 gap = DEFAULT_MIN_GAP, prefix = DEFAULT_PREFIX):
        self.limit = limit
        self.window = window
        self.gap = gap
        self.prefix = prefix

    def allow(self, key):
        ts = self._get_timestamp()
        cache_key = self._get_cache_key(key)
        _, card, lasts, *_ = REDIS.pipeline() \
            .zremrangebyscore(cache_key, "-inf", ts - self.window.total_seconds()) \
            .zcard(cache_key) \
            .zrevrange(cache_key, 0, 0, True) \
            .zadd(cache_key, {uuid.uuid4().int: ts}) \
            .expire(cache_key, int(self.window.total_seconds())) \
            .execute()
        return card < self.limit and \
            (len(lasts) == 0 or ts - lasts[0][1] >= self.gap.total_seconds())

    def _get_cache_key(self, key):
        return '%s.%s' % (self.prefix, key)


class RateLimiterTests(unittest.TestCase):
    def test_redis_counter_rate_limit_simple(self):
        timestamp = 0
        limiter = RedisCounterRateLimiter(limit=2)
        limiter._get_timestamp = lambda: timestamp
        self.assertTrue(limiter.allow('userA'))
        self.assertTrue(limiter.allow('userA'))
        self.assertFalse(limiter.allow('userA'))
        self.assertTrue(limiter.allow('userB'))

        timestamp = 1
        self.assertTrue(limiter.allow('userA'))

    def test_tokens_bucket_rate_limit_simple(self):
        timestamp = 0
        limiter = RedisTokensBucketRateLimiter(rate=2, capacity=10)
        limiter._get_timestamp = lambda: timestamp
        for _ in range(10):
            self.assertTrue(limiter.allow('userA'))
        self.assertFalse(limiter.allow('userA'))
        self.assertTrue(limiter.allow('userB'))

        timestamp = 1
        self.assertTrue(limiter.allow('userA'))
        self.assertTrue(limiter.allow('userA'))
        self.assertFalse(limiter.allow('userA'))

    def test_concurrent_requests_limit_simple(self):
        timestamp = 0
        limiter = RedisConcurrentRequestsLimiter(limit=2, ttl=timedelta(seconds=10))
        limiter._get_timestamp = lambda: timestamp

        self.assertTrue(limiter.allow('userA', 0))
        self.assertTrue(limiter.allow('userA', 1))
        self.assertFalse(limiter.allow('userA', 2))
        self.assertTrue(limiter.allow('userB', 0))

        timestamp = 5
        limiter.after_request('userA', 0)
        self.assertTrue(limiter.allow('userA', 3))
        self.assertFalse(limiter.allow('userA', 4))

        timestamp = 10 # request 1 removed because of ttl
        self.assertTrue(limiter.allow('userA', 5))
        self.assertFalse(limiter.allow('userA', 6))

    def test_rolling_window_rate_limit_simple(self):
        timestamp = 0
        limiter = RedisRollingWindowRateLimiter(limit=2, 
                window=timedelta(seconds=5), gap=timedelta(seconds=1))
        limiter._get_timestamp = lambda: timestamp
        self.assertTrue(limiter.allow('userA'))
        self.assertTrue(limiter.allow('userB'))
        timestamp = 1; self.assertTrue(limiter.allow('userA'))
        timestamp = 2; self.assertFalse(limiter.allow('userA'))
        timestamp = 6 # 2 requests cleared
        self.assertTrue(limiter.allow('userA'))
        self.assertFalse(limiter.allow('userA'))


if __name__ == '__main__':
    unittest.main()
