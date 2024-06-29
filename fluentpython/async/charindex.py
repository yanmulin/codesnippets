import unicodedata
import sys
from collections import defaultdict

Char = str
Index = defaultdict[str, set[Char]]

STOP_CODE = sys.maxunicode + 1

def tokenize(s: str):
    for token in s.upper().replace('-', ' ').split():
        yield token

class InvertedIndex:
    def __init__(self, start: int = 32, stop: int = STOP_CODE):
        entries = defaultdict(set)
        for char in (chr(i) for i in range(start, stop)):
            name = unicodedata.name(char, '')
            if name:
                for token in tokenize(name):
                    entries[token].add(char)
        self.entries = entries
    
    def search(self, query: str) -> set[str]:
        if words := list(tokenize(query)):
            found = self.entries[words[0]]
            return found.intersection(*[word for word in words[1:]])
        else:
            return set()
