import unittest

def next_permutation(arr):
    i = len(arr) - 1
    while i > 0 and arr[i-1] >= arr[i]:
        i -= 1
    if i == 0: return list(reversed(arr))
    i -= 1
    j = len(arr) - 1
    while j > i and arr[i] >= arr[j]:
        j -= 1
    arr[i], arr[j] = arr[j], arr[i]
    arr[i+1:] = reversed(arr[i+1:])
    return arr


class TestCase(unittest.TestCase):
    def test_simple(self):
        self.assertSequenceEqual(next_permutation([1, 2, 3, 4]), [1, 2, 4, 3])
        self.assertSequenceEqual(next_permutation([1, 2, 4, 3]), [1, 3, 2, 4])
        self.assertSequenceEqual(next_permutation([1, 3, 2, 4]), [1, 3, 4, 2])
        self.assertSequenceEqual(next_permutation([1, 4, 3, 2]), [2, 1, 3, 4])
        self.assertSequenceEqual(next_permutation([4, 3, 2, 1]), [1, 2, 3, 4])


if __name__ == '__main__':
    unittest.main()