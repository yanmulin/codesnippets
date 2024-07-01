from threading import Lock, RLock, Thread, Event

class KVStore:
    def __init__(self):
        self._lock = Lock()
        self._data = {}
    
    def get(self, key):
        with self._lock:
            return self._data.get(key)

    def put(self, key, value):
        with self._lock:
            self._data[key] = value


class ShardedKVStore:
    def __init__(self, num_shards=5):
        self._shards = [KVStore() for _ in range(num_shards)]
    
    def _get_shard(self, key):
        index = hash(key) % len(self._shards)
        return self._shards[index]

    def get(self, key):
        return self._get_shard(key).get(key)
    
    def put(self, key, value):
        self._get_shard(key).put(key, value)


class WriteAheadLog:
    def __init__(self, filepath):
        self._lock = Lock()
        self._fp = open(filepath, 'a+')

    def append(self, key, value):
        with self._lock:
            self._fp.seek(0, 2)
            self._fp.writelines(['BEGIN', key, value, 'COMMIT'])
            self._fp.flush()

    def recover(self, kv_store):
        with self._lock:
            self._fp.seek(0, 0)
            lines = self._fp.readlines()
            for log in zip(*[iter(lines)] * 4):
                if len(log) != 4: continue
                if log[0] != 'BEGIN': continue
                if log[-1] != 'COMMIT': continue
                kv_store.put(log[1], log[2])
    
    def clear(self):
        with self._lock:
            self._fp.truncate(0)


class SnapshotManager:
    def __init__(self, filepath, kv_store, lock):
        self._filepath = filepath
        self._lock = lock
        self._kv_store = kv_store

    def snapshot(self):
        with self._lock, open(self._filepath) as fp:
            it = iter(self._kv_store)
            for key, value in it:
                fp.writelines([key, value])

    def recover(self):
        with self._lock, open(self._filepath) as fp:
            lines = fp.readlines()
            for key, value in zip(*[iter(lines)] * 2):
                self._kv_store.put(key, value)


class SnapshotDaemon(Thread):
    def __init__(self, wal, snapshot_manager):
        self._wal = wal
        self._snapshot_manager = snapshot_manager
        self._stopped = Event()

    def run(self):
        while self._stopped.wait(3 * 60):
            self._snapshot_manager.snapshot()
            self._wal.clear()


class DurableKVStore:
    def __init__(self):
        self._lock = RLock()
        self._data = {}
        self._wal = WriteAheadLog('wal000.log')
        snapshot_manager = SnapshotManager('snapshot000.txt', self, self._lock)
        snapshot_manager.recover()
        self._wal.recover()

        self._snapshot_daemon = SnapshotDaemon(self._wal, snapshot_manager)
        self._snapshot_daemon.start()

    def get(self, key):
        with self._lock:
            return self._data.get(key)
    
    def put(self, key, value):
        with self._lock:
            self._wal.append(key, value)
            self._data[key] = value

    def __iter__(self):
        return iter(self._data.items())