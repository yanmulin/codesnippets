import json
import inspect

JSON_PATH = './osconfeed.json'

class Record:

    __index = None
    
    def __init__(self, **kwargs):
        self.__dict__.update(kwargs)

    def __repr__(self):
        cls_name = self.__class__.__name__
        return f'<{cls_name} serial={self.serial}>'

    @staticmethod
    def fetch(key):
        if Record.__index == None:
            Record.__index = load()
        return Record.__index[key]

class Event(Record):
    def __repr__(self):
        if hasattr(self, 'name'):
            cls_name = self.__class__.__name__
            return f'<{cls_name} {self.name!r}>'
        else:
            return super().__repr__()
        
    @property
    def venue(self):
        key = f'venue.{self.venue_serial}'
        return self.__class__.fetch(key)
    
    @property
    def speakers(self):
        speaker_serials = self.__dict__['speakers']
        fetch = self.__class__.fetch
        return [fetch(f'speaker.{serial}') for serial in speaker_serials]


def load(path: str = JSON_PATH):
    records = {}
    with open(path) as fp:
        raw = json.load(fp)
    for key, collection in raw['Schedule'].items():
        record_type = key[:-1]
        cls_name = record_type.capitalize()
        cls = globals().get(cls_name, Record)
        if inspect.isclass(cls) and issubclass(cls, Record):
            factory = cls
        else:
            factory = Record
        for item in collection:
            record = factory(**item)
            records[f'{record_type}.{record.serial}'] = record
    return records


def test_records():
    records = load()
    speaker = records['speaker.3471']
    assert (speaker.name, speaker.twitter) == ('Anna Martelli Ravenscroft', 'annaraven')

def test_linked():
    records = load()
    event = records['event.33950']
    speakers = [speaker.name for speaker in event.speakers]
    assert speakers == ['Anna Martelli Ravenscroft', 'Alex Martelli']