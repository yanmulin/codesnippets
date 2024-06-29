import functools
import operator
from collections.abc import Iterable
from typing import overload, TypeVar, Union

T = TypeVar('T')
S = TypeVar('S')

@overload
def mysum(it: Iterable[T]) -> Union[T, int]: ...

@overload
def mysum(it: Iterable[T], /, start: S) -> Union[T, S]: ...

def mysum(it, /, start=0):
    return functools.reduce(operator.add, it, start)

def test_mysum():
    assert mysum(range(5)) == 10
    assert mysum(range(5), 1) == 11