from functools import singledispatch
from collections import abc
import numbers
import html
import fractions
import decimal

@singledispatch
def htmlize(obj: object) -> str:
    content = html.escape(repr(obj))
    return f'<pre>{content}</pre>'

@htmlize.register
def _(text: str) -> str:
    content = html.escape(text.replace('\n', '<br/>\n'))
    return f'<p>{content}</p>'

@htmlize.register
def _(seq: abc.Sequence) -> str:
    lis = [f'<li>{htmlize(item)}</li>' for item in seq]
    return '\n'.join(lis)

@htmlize.register
def _(n: numbers.Integral) -> str:
    return f'<pre>{n}</pre>'

@htmlize.register
def _(b: bool) -> str:
    return f'<pre>{b}</pre>'

@htmlize.register(fractions.Fraction)
def _(x) -> str:
    frac = fractions.Fraction(x)
    return f'<pre>{frac.numerator}/{frac.denominator}</pre>'

@htmlize.register(decimal.Decimal)
@htmlize.register(float)
def _(x) -> str:
    frac = fractions.Fraction(x).limit_denominator()
    return f'<pre>{x} ({frac.numerator}/{frac.denominator})</pre>'

class RandomObj:
    def __repr__(self):
        typename = type(self).__name__
        return f'{typename}()'

def test_htmlize():
    assert htmlize(RandomObj()) == '<pre>RandomObj()</pre>'
    assert htmlize('text') == '<p>text</p>'
    assert htmlize((RandomObj(), RandomObj())) == '<li><pre>RandomObj()</pre></li>\n'\
        '<li><pre>RandomObj()</pre></li>'
    assert htmlize(1) == '<pre>1</pre>'
    assert htmlize(True) == '<pre>True</pre>'
    assert htmlize(fractions.Fraction(1, 5)) == '<pre>1/5</pre>'
    assert htmlize(0.2) == '<pre>0.2 (1/5)</pre>'
    assert htmlize(decimal.Decimal('0.5')) == '<pre>0.5 (1/2)</pre>'