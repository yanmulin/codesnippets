import unittest

class UnionFind:
    def __init__(self, n):
        self.parents = [i for i in range(n)]
        self.sizes = [1 for _ in range(n)]
    
    def union(self, x, y):
        rx, ry = self.find(x), self.find(y)
        if rx != ry:
            self.parents[rx] = ry
            self.sizes[ry] += self.sizes[rx]
    
    def find(self, x):
        if self.parents[x] != x:
            self.parents[x] = self.find(self.parents[x])
        return self.parents[x]


class UnionFindTests(unittest.TestCase):
    def test_simple(self):
        uf = UnionFind(5)
        uf.union(1, 4)
        self.assertEqual(uf.find(1), uf.find(4))
        uf.union(1, 2)
        self.assertEqual(uf.find(2), uf.find(4))
        self.assertEqual(max(uf.sizes), 3)
        uf.union(1, 3)
        self.assertEqual(uf.find(2), uf.find(3))
        self.assertEqual(max(uf.sizes), 4)


if __name__ == '__main__':
    unittest.main()
        