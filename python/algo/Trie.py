import unittest
from collections import defaultdict


class TrieNode:
    def __init__(self):
        self.children = defaultdict(TrieNode)
        self.is_word = False


class Trie:
    def __init__(self):
        self.root = TrieNode()

    def insert(self, word: str) -> None:
        u = self.root
        for ch in word:
            u = u.children[ch]
        u.is_word = True

    def search(self, word: str) -> bool:
        u = self.root
        for ch in word:
            u = u.children.get(ch)
            if u is None: return False
        return u.is_word

    def startsWith(self, prefix: str) -> bool:
        u = self.root
        for ch in prefix:
            u = u.children.get(ch)
            if u is None: return False
        return True


class TrieTest(unittest.TestCase):
    def test_simple(self):
        trie = Trie()
        trie.insert('apple')
        self.assertTrue(trie.search('apple'))
        self.assertTrue(trie.startsWith('app'))
        self.assertFalse(trie.search('app'))
        trie.insert('app')
        self.assertTrue(trie.search('app'))


if __name__ == '__main__':
    unittest.main()