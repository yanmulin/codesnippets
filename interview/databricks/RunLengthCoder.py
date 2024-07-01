import unittest
from collections import deque


class RunLengthObj:
    def __init__(self, x, count):
        self.x = x
        self.count = count


class BasicObj:
    def __init__(self, *args):
        if len(args) != 8:
            raise ValueError(args + ' should be of size 8')
        self.data = args


class DataWriter:
    def __init__(self):
        self.data = []
    
    def append(self, x):
        if isinstance(x, BasicObj):
            self.data.extend(str(elm) for elm in x.data)
        elif isinstance(x, RunLengthObj):
            self.data.append('X%d/%d' % (x.x, x.count))
    
    def __str__(self):
        return ' '.join(self.data)


class Pair:
    def __init__(self, x, count=1):
        self.x = x
        self.count = count
    
    def __repr__(self):
        return 'Pair(%d,%d)' % (self.x, self.count)


# https://www.1point3acres.com/bbs/thread-907861-1-1.html
class RunLengthCoder:

    def __init__(self):
        self.total = 0
        self.prev = deque()

    def encode(self, x, writer):
        if len(self.prev) > 0 and self.prev[-1].x == x:
            self.prev[-1].count += 1
        else:
            self.flush(writer)
            self.prev.append(Pair(x))
        self.total += 1

    def decode(self, x, writer):
        if isinstance(x, BasicObj):
            for elm in x.data:
                writer.append(elm)
        elif isinstance(x, RunLengthObj):
            for _ in range(x.count):
                writer.append(x.x)

    def flush(self, writer):
        while self.total >= 8:
            pair = self.prev.popleft()
            if pair.count >= 8:
                writer.append(RunLengthObj(pair.x, pair.count))
                self.total -= pair.count
            else:
                data = [pair.x] * pair.count
                while len(data) < 8:
                    nxt = self.prev.popleft()
                    data.extend([nxt.x] * nxt.count)
                writer.append(BasicObj(*data[:8]))
                if len(data) > 8:
                    self.prev.append(Pair(data[-1], len(data) - 8))
                self.total -= 8


class RunLengthCoderTests(unittest.TestCase):
    def _encode(self, data):
        coder = RunLengthCoder()
        writer = DataWriter()
        for x in data:
            coder.encode(x, writer)
        coder.flush(writer)
        return str(writer)
        
    def test_encode_simple(self):
        self.assertEqual(self._encode([1, 2, 3, 4, 5, 6, 7, 8]), '1 2 3 4 5 6 7 8')
        self.assertEqual(self._encode([1, 1, 1, 1, 1, 1, 1, 1]), 'X1/8')
        self.assertEqual(self._encode([1, 1, 1, 1, 1, 1, 1, 1, 1, 1]), 'X1/10')
        self.assertEqual(self._encode([1, 1, 1, 1, 1, 1, 1, 8]), '1 1 1 1 1 1 1 8')
        self.assertEqual(
            self._encode([1, 2, 3, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1]), 
            '1 2 3 4 1 1 1 1 X1/8'
        )


if __name__ == '__main__':
    unittest.main()