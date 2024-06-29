import pytest
from collections import abc
import keyword
import json


class FrozenJson:
    def __new__(cls, arg):
        if isinstance(arg, abc.Mapping):
            return super().__new__(cls)
        elif isinstance(arg, abc.MutableSequence):
            return [cls(ele) for ele in arg]
        else: # primitive types, such as int, str, ...
            return arg
    
    def __init__(self, mapping):
        self.__data = {}
        for key, value in mapping.items():
            if keyword.iskeyword(key):
                key += '_'
            elif not key.isidentifier():
                key = 'attr_' + key
            self.__data[key] = value
        
    def __getattr__(self, key):
        try:
            return getattr(self.__data, key) # .keys(), .items(), ...
        except AttributeError:
            return FrozenJson(self.__data[key])


def test_osconfeed():
    with open('./osconfeed.json') as fp:
        raw = json.load(fp)
    feed = FrozenJson(raw)
    
    assert sorted(feed.Schedule.keys()) == ['conferences', 'events', 'speakers', 'venues']
    
    assert len(feed.Schedule.speakers) == 357
    assert len(feed.Schedule.events) == 484
    assert len(feed.Schedule.conferences) == 1
    assert len(feed.Schedule.venues) == 53
    
    assert feed.Schedule.speakers[-1].name == 'Carina C. Zona'
    
    with pytest.raises(KeyError):
        feed.Schedule.events[40].flavor

def test_invalid_json():
    student = FrozenJson({'name': 'Jim Bo', 'class': 1982, '2be': 'or not'})
    assert student.class_ == 1982
    assert student.attr_2be == 'or not'