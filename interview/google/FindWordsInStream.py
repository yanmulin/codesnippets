'''
Google Virtual Onsite Round 2 - 9/19/2024
Find Words in a Stream

*input*
words: a list of words
a stream of characters

*output*
returns the word if all characters appear in the stream

*example*
words = ['foo', 'bar']
stream = 'a',  'b',  'c',  'f',  'b',  'r',   'o',  'o', ...
return = null, null, null, null, null, 'bar', null, 'foo', ...

'''

from collections import Counter, defaultdict
import unittest


class Solver:
    def __init__(self, words):
        self.chars_counter = {w: defaultdict(int) for w in words}
        self.chars_freq = {w: Counter(w) for w in words}
        self.char_words = defaultdict(set)
        for w in words:
            for ch in w:
                self.char_words[ch].add(w)
    
    def accept(self, char):
        ans = []
        for w in self.char_words[char]:
            self.chars_counter[w][char] += 1
            if self._is_word_found(self.chars_counter[w], self.chars_freq[w]):
                self._remove_word(w)
                ans.append(w)

        if not ans: return None
        elif len(ans) == 1: return ans[0]
        else: return ans
    
    def _is_word_found(self, counter, freq):
        for ch, f in freq.items():
            if ch not in counter or counter[ch] < f:
                return False
        return True
    
    def _remove_word(self, word):
        for ch in word:
            for update in self.char_words[ch]:
                new_count = self.chars_counter[update][ch] - self.chars_freq[word][ch]
                self.chars_counter[update][ch] = max(0, new_count)


class SolverTests(unittest.TestCase):
    def test_solution(self):
        s = Solver(['foo', 'bar'])
        self.assertEqual(s.accept('a'), None)
        self.assertEqual(s.accept('b'), None)
        self.assertEqual(s.accept('c'), None)
        self.assertEqual(s.accept('f'), None)
        self.assertEqual(s.accept('b'), None)
        self.assertEqual(s.accept('r'), 'bar')
        self.assertEqual(s.accept('o'), None)
        self.assertEqual(s.accept('o'), 'foo')
        self.assertEqual(s.accept('r'), None)
        self.assertEqual(s.accept('a'), 'bar')


if __name__ == '__main__':
    unittest.main()