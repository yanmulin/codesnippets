'''
Fibonacci Tree
https://inst.eecs.berkeley.edu/~cs61bl/r//cur/trees/fibonacci-tree.html?topic=lab15.topic&step=7
'''
import unittest
from collections import deque

class TreeNode:
    def __init__(self, val, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right


def fibtree(n):
    if n == 0: return TreeNode(0)
    if n == 1: return TreeNode(1)
    left, right = fibtree(n - 1), fibtree(n - 2)
    return TreeNode(left.val + right.val, left, right)
    

class FibTreeTests(unittest.TestCase):
    def _nodes(self, root):
        result = []
        q = deque()
        q.append(root)
        while len(q) > 0:
            for _ in range(len(q)):
                u = q.popleft()
                if u == None: result.append(None)
                else:
                    result.append(u.val)
                    q.append(u.left); q.append(u.right)
        while result[-1] == None: result.pop()
        return result

    def test_simple(self):
        expected = [3, 2, 1, 1, 1, 1, 0, 1, 0]
        nodes = self._nodes(fibtree(4))
        self.assertEqual(expected, nodes)


if __name__ == '__main__':
    unittest.main()