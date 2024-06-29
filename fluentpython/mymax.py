import pytest
from collections.abc import Iterable
from typing import overload, TypeVar, Protocol, Any, Callable, Union

MISSING = object()

class SupportsLessThan(Protocol):
    def __lt__(self, other: Any) -> bool: ...

T = TypeVar('T')
LT = TypeVar('LT', bound=SupportsLessThan)
DT = TypeVar('DT')

@overload
def mymax(__arg1: LT, __arg2: LT, *_args: LT, key: None = ...) -> LT: ...

@overload
def mymax(__iterable: Iterable[LT], *, key: None = ...) -> LT: ...

@overload
def mymax(__arg1: T, __arg2: T, *_args: T, key: Callable[[T], LT]) -> T: ...
@overload
def mymax(__iterable: Iterable[LT], *, key: Callable[[T], LT]) -> T: ...

@overload
def mymax(__iterable: Iterable[LT], *, key: None = ..., default: DT) -> Union[DT, LT]: ...

@overload
def mymax(__iterable: Iterable[T], *, key: Callable[[T], LT], default: DT) -> Union[DT, LT]: ...

def mymax(first, *args, key=None, default=MISSING):
    if args:
        series = iter(args)
        candidate = first
    else:
        series = iter(first)
        try:
            candidate = next(series)
        except StopIteration:
            if default is not MISSING:
                return default
            else:
                raise ValueError('max() arg is an empty sequence') from None
    if key is None:
        for current in series:
            if candidate < current:
                candidate = current
    else:
        candidate_key = key(candidate)
        for current in series:
            current_key = key(current)
            if candidate_key < current_key:
                candidate = current
                candidate_key = current_key
    return candidate

def test_mymax():
    assert mymax([1]) == 1
    assert mymax([1, 2, 3]) == 3
    assert mymax(1, 2, 3) == 3
    assert mymax(['a', 'aa', 'aaa'], key=lambda s: len(s)) == 'aaa'
    assert mymax('a', 'aa', 'aaa', key=lambda s: len(s)) == 'aaa'
    assert mymax([], default=0) == 0
    
    with pytest.raises(ValueError):
        mymax([])