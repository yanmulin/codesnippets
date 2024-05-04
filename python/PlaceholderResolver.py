import unittest
from abc import ABC, abstractmethod
from typing import override, List

class UnresolvedPlaceholder(BaseException):
    def __init__(self, text):
        super().__init__(f'unable to resolve "{text}"')


class ResolveContext:
    def __init__(self, resolver, prefix: str, suffix: str):
        self.resolver = resolver
        self.prefix = prefix
        self.suffix = suffix

    def parse(self, text):
        return self.resolver.parser.parse(text, False)

    def resolve_placeholder(self, key):
        return self.resolver.properties.get(key)

    def handle_unresolved(self, text):
        if self.resolver.ignore_unresolved:
            return self._to_placeholder_text(text)
        raise UnresolvedPlaceholder(text)

    def _to_placeholder_text(self, text):
        return f'{self.prefix}{text}{self.suffix}'


class Part(ABC):
    @classmethod
    def _classname(cls):
        return cls.__name__

    def __repr__(self):
        attrs_str = ', '.join(f'{k}={v}' for k,v in self.__dict__.items())
        return f'{self._classname()}({attrs_str})'

    @abstractmethod
    def resolve(self, context: ResolveContext): ...


@Part.register
class TextPart:
    def __init__(self, text):
        self.text = text

    def resolve(self, context: ResolveContext):
        return self.text


@Part.register
class SimplePlaceholderPart(Part):
    def __init__(self, text: str, key: str, fallback: str):
        self.text = text
        self.key = key
        self.fallback = fallback

    def resolve(self, context: ResolveContext):
        value = context.resolve_placeholder(self.key)
        if value is None:
            return self.fallback if self.fallback is not None else context.handle_unresolved(self.text)
        nested_parts = context.parse(value)
        return ''.join(p.resolve(context) for p in nested_parts)


@Part.register
class NestedPlaceholderPart(Part):
    def __init__(self, text, parts: List[Part], defaults):
        self.text = text
        self.parts = parts
        self.defaults = defaults

    def resolve(self, context: ResolveContext):
        value = context.resolve_placeholder(''.join(p.resolve(context) for p in self.parts))
        if value is None and self.defaults is not None:
            value = ''.join(p.resolve(context) for p in self.defaults)
        return value if value is not None else context.handle_unresolved(self.text)


class PlaceholderParser:
    def __init__(self, prefix: str, suffix: str, seperator: str, escape: str):
        self.prefix = prefix
        self.suffix = suffix
        self.separator = seperator
        self.escape = escape

    def _next_start_index(self, value: str, start: int) -> int:
        return value.find(self.prefix, start)
    
    def _next_end_index(self, value: str, start: int) -> int:
        index = start
        nested = 0
        while index < len(value):
            if value.startswith(self.prefix, index):
                nested += 1
                index += len(self.prefix)
            elif value.startswith(self.suffix, index) and nested == 0:
                return index
            elif value.startswith(self.suffix, index):
                nested -= 1
                index += len(self.suffix)
            else:
                index += 1
        return -1
    
    def _is_escape(self, value: str, index: int) -> bool:
        return index > 0 and value[index - 1] == self.escape
    
    def _add_text(self, value: str, start: int, end: int, parts: List[Part]):
        if start >= end: return
        if len(parts) > 0 and isinstance(parts[-1], TextPart):
            parts[-1].text += value[start:end]
        else: 
            parts.append(TextPart(value[start:end]))

    def _create_simple_placeholder_part(self, value: str):
        index = value.find(self.separator)
        start = 0
        key = ''
        while index != -1 and self._is_escape(value, index):
            key += value[start:index-1] 
            start = index
            index = value.find(self.separator, index + 1)
        
        if index == -1:
            key += value[start:]
            return SimplePlaceholderPart(key, key, None)
        else:
            key += value[start:index]
            return SimplePlaceholderPart(key, key, value[index+1:])
    
    def _create_nested_placeholder_part(self, value: str, parts: List[Part]):
        keys = []
        defaults = []
        for i, part in enumerate(parts):
            if not isinstance(part, TextPart):
                keys.append(part)
            else:
                index = part.text.find(self.separator)
                while index != -1 and self._is_escape(part.text, index):
                    index = part.text.find(self.separator, index + 1)
                if index != -1:
                    self._add_text(part.text, index + 1, len(part.text), defaults)
                    defaults.extend(parts[i+1:])
                    return NestedPlaceholderPart(value, keys, defaults)
                else:
                    keys.append(part)
        return NestedPlaceholderPart(value, keys, None)

    def parse(self, value: str, nested: bool) -> List[Part]:
        parts: List[Part] = []
        index = 0
        start_index = self._next_start_index(value, index)
        if start_index == -1:
            parts.append(TextPart(value) if not nested else self._create_simple_placeholder_part(value))
            return parts
        
        while start_index != -1:
            end_index = self._next_end_index(value, start_index + len(self.prefix))
            if end_index == -1:
                self._add_text(value, index, start_index, parts)
                index = start_index
                start_index = self._next_start_index(value, start_index + len(self.prefix))
            elif self._is_escape(value, start_index):
                self._add_text(value, index, start_index - len(self.escape), parts)
                index = start_index
                start_index = self._next_start_index(value, start_index + len(self.prefix))
            else:
                self._add_text(value, index, start_index, parts)
                parts.extend(self.parse(value[start_index + len(self.prefix):end_index], True))
                index = end_index + len(self.suffix)
                start_index = self._next_start_index(value, end_index + len(self.suffix))

        self._add_text(value, index, len(value), parts)
        return [self._create_nested_placeholder_part(value, parts)] if nested else parts


class PlaceholderResolver:
    PREFIX = '${'
    SUFFIX = '}'
    SIMPLE_PREFIX = '}'
    SEPARATOR = ':'
    ESCAPE = '\\'

    def __init__(self, properties: dict[str, str], ignore_unresolved: bool = False):
        self.properties = properties
        self.parser = PlaceholderParser(self.PREFIX, self.SUFFIX, self.SEPARATOR, self.ESCAPE)
        self.ignore_unresolved = ignore_unresolved

    def resolve(self, value: str) -> str:
        parts = self.parser.parse(value, False)
        context = ResolveContext(self, self.PREFIX, self.SUFFIX)
        return ''.join(p.resolve(context) for p in parts)


class PlaceholderResolverTests(unittest.TestCase):

    def _run_test(self, properties: dict[str, str], testcases: tuple[tuple[str, str]]):
        resolver = PlaceholderResolver(properties, True)

        for value, expected in testcases:
            self.assertEqual(expected, resolver.resolve(value), f'resolving {value}')

    PLACEHOLDERS = (
        ('${firstName}', 'John'),
        ('$${firstName}', '$John'),
        ('}${firstName}', '}John'),
        ('${firstName}$', 'John$'),
        ('${firstName}}', 'John}'),
        ('${firstName} ${firstName}', 'John John'),
        ('First name: ${firstName}', 'First name: John'),
        ('${firstName} is the first name', 'John is the first name'),
        ('${first${nested1}}', 'John'),
        ('${${nested0}${nested1}}', 'John'),
    )
    
    def test_placeholders(self):
        properties = {
            'firstName': 'John',
            'nested0': 'first',
            'nested1': 'Name',
        }
        self._run_test(properties, self.PLACEHOLDERS)
        

    NESTED = (
        ('${p1}:${p2}', 'v1:v2'),
        ('${p3}', 'v1:v2'),
        ('${p4}', 'v1:v2'),
        ('${p5}', 'v1:v2:${bogus}'),
        ('${p0${p0}}', '${p0${p0}}'),
    )

    def test_nested(self):
        properties = {
            'p1': 'v1',
            'p2': 'v2',
            'p3': '${p1}:${p2}',
            'p4': '${p3}',
            'p5': '${p1}:${p2}:${bogus}',
        }
        self._run_test(properties, self.NESTED)

    DEFAULTS = (
        ('${invalid:John}', 'John'),
        ('${first${invalid:Name}}', 'John'),
        ('${invalid:${firstName}}', 'John'),
        ('${invalid:${${nested0}${nested1}}}', 'John'),
        ('${invalid:$${firstName}}', '$John'),
        ('${invalid: }${firstName}', ' John'),
        ('${invalid:}', ''),
        ('${:}', '')
    )

    def test_defaults(self):
        properties = {
			'firstName': 'John',
			'nested0': 'first',
			'nested1': 'Name',
        }
        self._run_test(properties, self.DEFAULTS)

    DEFAULTS_NESTED = (
        ('${p6}', 'v1:v2:def'),
        ('${invalid:${p1}:${p2}}', 'v1:v2'),
        ('${invalid:${p3}}', 'v1:v2'),
        ('${invalid:${p4}}', 'v1:v2'),
        ('${invalid:${p5}}', 'v1:v2:${bogus}'),
        ('${invalid:${p6}}', 'v1:v2:def')
    )

    def test_defaults_nested(self):
        properties = {
			'p1': 'v1',
			'p2': 'v2',
			'p3': '${p1}:${p2}',
			'p4': '${p3}',
			'p5': '${p1}:${p2}:${bogus}',
			'p6': '${p1}:${p2}:${bogus:def}',
        }
        self._run_test(properties, self.DEFAULTS_NESTED)

    ESCAPE = (
        ('\\${firstName}', '${firstName}'),
        ('First name: \\${firstName}', 'First name: ${firstName}'),
        ('$\\${firstName}', '$${firstName}'),
        ('\\}${firstName}', '\\}John'),
        ('${\\${test}}', 'John'),
        ('${p2}', '${p1:default}'),
        ('${p3}', '${p1:default}'),
        ('${p4}', 'adc${p1}'),
        ('${p5}', 'adcv1'),
        ('${p6}', 'adcdef${p1}'),
        ('${p7}', 'adc\\${'),
        ('${first\\:Name}', 'John'),
        ('${last\\:Name}', '${last:Name}'),
    )

    def test_escape(self):
        properties = {
            'firstName': 'John', 
            'first:Name': 'John', 
            'nested0': 'first', 
            'nested1': 'Name',
            '${test}': 'John',
            'p1': 'v1', 
            'p2': '\\${p1:default}', 
            'p3': '${p2}',
            'p4': 'adc${p0:\\${p1}}',
            'p5': 'adc${\\${p0}:${p1}}',
            'p6': 'adc${p0:def\\${p1}}',
            'p7': 'adc\\${',
        }
        self._run_test(properties, self.ESCAPE)


if __name__ == '__main__':
    unittest.main()