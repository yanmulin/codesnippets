import unittest
import bisect
from collections import defaultdict

'''
copy the snapshot in Iterator, memory in-efficient
'''
class CopySnapshotSet:
    class Iterator:
        def __init__(self, it):
            self.it = iter(list(it))
        
        def __next__(self):
            return next(self.it)

    def __init__(self):
        self.elms = set()
    
    def add(self, x):
        self.elms.add(x)

    def remove(self, x):
        self.elms.remove(x)

    def contains(self, x):
        return x in self.elms

    def __iter__(self):
        return self.Iterator(iter(self.elms))


'''
Record each element's add/remove action history, search the last action 
in history to determine if the element is in the given snapshot. Similar to 
https://leetcode.com/problems/snapshot-array/
'''
class HistorySnapshotSet:
    _ADD = 1
    _REMOVE = 2
    _MAX = 3

    class Iterator:
        def __init__(self, id, it):
            self.id = id
            self.it = it
        
        def __next__(self):
            while True:
                x, history = next(self.it)
                pi = bisect.bisect_right(history, (self.id, HistorySnapshotSet._MAX))
                if pi > 0 and history[pi - 1][1] == HistorySnapshotSet._ADD: 
                    return x

    def __init__(self):
        self.id = 0
        self.history = defaultdict(list)

    def add(self, x):
        self.history[x].append((self.id, self._ADD))

    def remove(self, x):
        if self.contains(x):
            self.history[x].append((self.id, self._REMOVE))

    def contains(self, x):
        return x in self.history and self.history[x][-1][1] == self._ADD

    def __iter__(self):
        self.id += 1
        # TODO: avoid the copy
        return self.Iterator(self.id - 1, iter(self.history.copy().items()))


class SnapshotIteratorTests(unittest.TestCase):
    def test_copy_snapshot_iterator(self):
        s = CopySnapshotSet()
        s.add(3); s.add(4); s.add(5); s.remove(5)
        it = iter(s)
        s.add(6); s.remove(3)
        expected = set([3, 4]); first = next(it)
        self.assertIn(first, expected); expected.remove(first)
        self.assertIn(next(it), expected)
        self.assertRaises(StopIteration, lambda: next(it))

    def test_history_snapshot_iterator(self):
        s = HistorySnapshotSet()
        s.add(3); s.add(4); s.add(5); s.remove(5)
        it = iter(s)
        s.add(6); s.remove(3)
        expected = set([3, 4]); first = next(it)
        self.assertIn(first, expected); expected.remove(first)
        self.assertIn(next(it), expected)
        self.assertRaises(StopIteration, lambda: next(it))


if __name__ == '__main__':
    unittest.main()