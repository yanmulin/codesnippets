import pytest
import re
import reprlib

RE_WORD = re.compile(r'\w+')

class Sentence:
    def __init__(self, text):
        self.text = text
        self.words = RE_WORD.findall(text)
        
    def __repr__(self):
        return f'Sentence({reprlib.repr(self.text)})'
    
    def __iter__(self):
        for word in self.words:
            yield word


def test_iterator():
    it = iter(Sentence('hello world'))
    assert next(it) == 'hello'
    assert next(it) == 'world'
    with pytest.raises(StopIteration):
        next(it)