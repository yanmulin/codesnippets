import pytest
from collections import abc
import json

class FrozenJson:
    def __init__(self, mapping):
        self.__data = dict(mapping)
        
    def __getattr__(self, key):
        try:
            return getattr(self.__data, key) # .keys(), .items(), ...
        except AttributeError:
            return FrozenJson.build(self.__data[key])

    @classmethod
    def build(cls, obj):
        if isinstance(obj, abc.Mapping):
            return FrozenJson(obj)
        elif isinstance(obj, abc.MutableSequence):
            return [FrozenJson(ele) for ele in obj]
        else: # primitive types, such as int, str, ...
            return obj

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