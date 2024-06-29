import pytest
import math
import reprlib
import operator
import itertools
import functools
from collections import abc
from array import array

class Vector:
    __match_args__ = ('x', 'y', 'z', 't')
    
    typecode = 'd'

    def __init__(self, components):
        self._components = array(self.typecode, components)
    
    def __iter__(self): # enable unpacking
        return iter(self._components)
    
    def __str__(self):
        return str(tuple(self))
    
    def __repr__(self):
        components = reprlib.repr(self._components)
        components = components[components.find('['):-1]
        typename = type(self).__name__
        return '{}({})'.format(typename, components)
    
    def __len__(self):
        return len(self._components)
    
    def __getitem__(self, key):
        if isinstance(key, slice):
            cls = type(self)
            return cls(self._components[key])
        index = operator.index(key)
        return self._components[index]
    
    def __getattr__(self, name):
        cls = type(self)
        try:
            pos = cls.__match_args__.index(name)
        except ValueError:
            pos = -1
        if 0 <= pos and pos < len(self._components):
            return self._components[pos]
        raise AttributeError(f'{cls.__name__!r} object has no attribute {name!r}')

    def __setattr__(self, name, value):
        cls = type(self)
        if len(name) == 1:
            if name in cls.__match_args__:
                error = 'readonly attribute {attr_name!r}'
            elif 'a' <= name and name <= 'z':
                error = 'cannot set attributes \'a\' to \'z\' in {cls_name!r}'
            else:
                error = ''
            if error:
                raise AttributeError(
                    error.format(
                        cls_name=cls.__name__,
                        attr_name=name
                    )
                )
        super().__setattr__(name, value)
    
    def __eq__(self, other):
        return len(self) == len(other) and all(a == b for a, b in zip(self, other))
    
    def __abs__(self):
        return math.hypot(*self)
    
    def __bool__(self):
        return bool(abs(self))
    
    def __bytes__(self):
        return bytes([ord(self.typecode)]) + \
            bytes(array(self.typecode, self))
            
    @classmethod
    def frombytes(cls, octects):
        typecode = chr(octects[0])
        memv = memoryview(octects[1:]).cast(typecode)
        return cls(memv)
    
    def angle(self, n):
        r = math.hypot(*self[n:])
        a = math.atan2(r, self[n-1])
        if n == len(self) - 1 and self[-1] < 0:
            return math.pi * 2 -  a
        else:
            return a
        
    def angles(self):
        return (self.angle(n) for n in range(1, len(self)))
    
    def __format__(self, fmt_spec=''):
        if fmt_spec.endswith('h'):
            fmt_spec = fmt_spec[:-1]
            coords = itertools.chain([abs(self)], self.angles())
            output_fmt = '<{}>'
        else:
            coords = self
            output_fmt = '({})'
        components = (format(c, fmt_spec) for c in coords)
        return output_fmt.format(', '.join(components))

    def __hash__(self):
        hashes = (hash(x) for x in self)
        return functools.reduce(operator.xor, hashes, 0)
    
    def __add__(self, other):
        try:
            pairs = itertools.zip_longest(self, other, fillvalue=0.0)
            return Vector(a + b for a, b in pairs)
        except TypeError:
            return NotImplemented
    
    def __radd__(self, other):
        return self + other
    
    def __mul__(self, scalar):
        try:
            factor = float(scalar)
        except TypeError:
            return NotImplemented
        return Vector(n * factor for n in self)
    
    def __rmul__(self, scalar):
        return self * scalar
    
    def __matmul__(self, other):
        if isinstance(other, abc.Sized) and isinstance(other, abc.Iterable):
            if len(self) != len(other):
                raise ValueError('@ requires vectors of equal length.')
            else:
                return sum(a * b for a, b in zip(self, other))
        else:
            return NotImplemented
        
    def __rmatmul__(self, other):
        return self @ other
        

@pytest.fixture
def v():
    return Vector((3, 4))

@pytest.fixture
def zero_v():
    return Vector((0, 0))

def test_n_dim():
    assert Vector((1, 2, 3, 4, 5))

def test_basic(v, zero_v):
    assert str(v)
    assert repr(v)
    assert eval(repr(v)) == v
    assert abs(v) == 5.0
    assert bool(v)
    assert not bool(zero_v)
    assert len(v) == 2

def test_to_from_bytes(v):
    assert Vector.frombytes(bytes(v)) == v

def test_formatting(v):
    assert format(v) == '(3.0, 4.0)'
    assert format(v, '.3f') == '(3.000, 4.000)'
    assert format(v, '.3e') == '(3.000e+00, 4.000e+00)'
    assert format(v, '.2fh') == '<5.00, 0.93>'

def test_immutability(v):
    with pytest.raises(AttributeError):
        v.x = 1
    with pytest.raises(AttributeError):
        v.y = 1
        
def test_hashable(v):
    assert hash(v) == hash(Vector(v))

def test_attrs(v):
    assert v.x == 3
    assert v.y == 4
    with pytest.raises(AttributeError):
        print(v.z)

    with pytest.raises(AttributeError):
        print(v.f)
    
    with pytest.raises(AttributeError):
        v.f = 1
    
    v.attr = 5
    assert v.attr == 5

def test_indexing(v):
    assert v[0] == 3
    assert isinstance(v[:], Vector)
    assert v[:] == Vector(v)
    
def test_operator_overloading(v):
    assert v + Vector((1, 2, 3, 4, 5)) == Vector((4, 6, 3, 4, 5))
    assert v + range(5) == Vector((3, 5, 2, 3, 4))
    with pytest.raises(TypeError):
        v + 1
        
    assert v * 2 == Vector((6, 8))
    with pytest.raises(TypeError):
        v * (1+2j)

    assert v @ Vector((1, 2)) == 11
    assert v @ (1, 2,) == 11
    with pytest.raises(TypeError):
        v @ 1
    with pytest.raises(ValueError):
        v @ (1, 2, 3)