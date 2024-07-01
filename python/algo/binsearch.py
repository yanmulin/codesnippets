import unittest


def left_binsearch(lst, checker):
    l, r = 0, len(lst) - 1
    while l < r:
        mid = (l + r + 1) >> 1
        if checker(lst[mid]): l = mid
        else: r = mid - 1
    return l


def right_binsearch(lst, checker):
    l, r = 0, len(lst) - 1
    while l < r:
        mid = (l + r) >> 1
        if checker(lst[mid]): r = mid
        else: l = mid + 1
    return l


class BinSearchTest(unittest.TestCase):
    def test_left_binsearch(self):
        self.assertEqual(left_binsearch([1, 3, 5, 7, 9], lambda x: x <= 3), 1)
        self.assertEqual(left_binsearch([1, 3, 5, 7, 9], lambda x: x <= 1), 0)
        self.assertEqual(left_binsearch([1, 3, 5, 7, 9], lambda x: x <= 6), 2)
        self.assertEqual(left_binsearch([1, 3, 5, 7, 9], lambda x: x <= 0), 0)
        self.assertEqual(left_binsearch([1, 3, 5, 7, 9], lambda x: x <= 10), 4)

    def test_right_binsearch(self):
        self.assertEqual(right_binsearch([1, 3, 5, 7, 9], lambda x: x >= 3), 1)
        self.assertEqual(right_binsearch([1, 3, 5, 7, 9], lambda x: x >= 1), 0)
        self.assertEqual(right_binsearch([1, 3, 5, 7, 9], lambda x: x >= 6), 3)
        self.assertEqual(right_binsearch([1, 3, 5, 7, 9], lambda x: x >= 0), 0)
        self.assertEqual(right_binsearch([1, 3, 5, 7, 9], lambda x: x >= 10), 4)


if __name__ == '__main__':
    unittest.main()