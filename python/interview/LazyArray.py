import unittest
from unittest.mock import MagicMock

# https://www.1point3acres.com/bbs/thread-832280-1-1.html
class LazyArray:
    def __init__(self, data, mappers=None):
        self._data = data
        self._mappers = mappers or []

    def map(self, mapper):
        return LazyArray(self._data, self._mappers + [mapper])

    def indexOf(self, value):
        for i, x in enumerate(self._data):
            for mapper in self._mappers:
                x = mapper(x)
            if x == value: return i
        return -1


class LazyArrayTests(unittest.TestCase):
    def test_simple(self):
        self.assertEqual(LazyArray([]).map(lambda x: x + 1).indexOf(10), -1)
        self.assertEqual(LazyArray([1, 2]).map(lambda x: x * 2).indexOf(4), 1)
        self.assertEqual(LazyArray([1, 2]).map(lambda x: x * 2).indexOf(100), -1)

        arr = LazyArray([1, 2, 3, 4, 5])
        self.assertEqual(arr.map(lambda x: x ** 2).indexOf(16), 3)
        self.assertEqual(arr.map(lambda x: x ** 3).map(lambda x: x - 2).indexOf(25), 2)

    def test_lazyness(self):
        mock_mapper = MagicMock(return_value=3)
        
        arr = LazyArray([1, 2, 3, 4, 5]).map(mock_mapper)
        mock_mapper.assert_not_called()

        arr.indexOf(0)
        mock_mapper.assert_called()


if __name__ == '__main__':
    unittest.main()