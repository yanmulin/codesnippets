import pytest
import math
import sys
from array import array

class Vector2d:
    __match_args__ = ('x', 'y')
    __slots__ = ('__x', '__y')
    
    typecode = 'd'

    def __init__(self, x, y):
        self.__x = float(x)
        self.__y = float(y)
        
    @property
    def x(self):
        return self.__x
    
    @property
    def y(self):
        return self.__y
    
    def __iter__(self): # enable unpacking
        return (i for i in (self.__x, self.__y))
    
    def __str__(self):
        return str(tuple(self))
    
    def __repr__(self):
        typename = type(self).__name__
        return '{}({!r}, {!r})'.format(typename, *self)
    
    def __eq__(self, other):
        return tuple(self) == tuple(other)
    
    def __abs__(self):
        return math.hypot(self.__x, self.__y)
    
    def __bool__(self):
        return bool(abs(self))
    
    def __bytes__(self):
        return bytes([ord(self.typecode)]) + \
            bytes(array(self.typecode, self))
            
    @classmethod
    def frombytes(cls, octects):
        typecode = chr(octects[0])
        memv = memoryview(octects[1:]).cast(typecode)
        return cls(*memv)
    
    def angle(self):
        return math.atan2(self.__y, self.__x)
    
    def __format__(self, fmt_spec=''):
        if fmt_spec.endswith('p'):
            fmt_spec = fmt_spec[:-1]
            coords = (abs(self), self.angle())
            output_fmt = '<{}, {}>'
        else:
            coords = self
            output_fmt = '({}, {})'
        components = (format(c, fmt_spec) for c in coords)
        return output_fmt.format(*components)

    def __hash__(self):
        return hash(self.__x) ^ hash(self.__y)

@pytest.fixture
def v():
    return Vector2d(3, 4)

@pytest.fixture
def zero_v():
    return Vector2d(0, 0)

def test_basic(v, zero_v):
    assert str(v)
    assert repr(v)
    assert eval(repr(v)) == v
    assert abs(v) == 5.0
    assert bool(v)
    assert not bool(zero_v)

def test_to_from_bytes(v):
    assert Vector2d.frombytes(bytes(v)) == v

def test_formatting(v):
    assert format(v) == '(3.0, 4.0)'
    assert format(v, '.3f') == '(3.000, 4.000)'
    assert format(v, '.3e') == '(3.000e+00, 4.000e+00)'
    assert format(v, '.2fp') == '<5.00, 0.93>'

def test_immutability(v):
    with pytest.raises(AttributeError):
        v.x = 1
    with pytest.raises(AttributeError):
        v.y = 1
        
def test_hashable(v):
    assert hash(v) == hash(Vector2d(v.x, v.y))

@pytest.mark.skipif(sys.version_info < (3, 10), reason="requires python3.10 or higher")
def test_pattern_matching(v):
    match v:
        case Vector2d(x=v.x):
            pass
        case _:
            pytest.fail()
    
    match v:
        case Vector2d(y=v.y):
            pass
        case _:
            pytest.fail()
            
    match v:
        case Vector2d(v.x, v.y):
            pass
        case _:
            pytest.fail()