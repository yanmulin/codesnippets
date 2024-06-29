import pytest

def tag(name, *content, class_=None, **attrs):
    if class_:
        attrs['class'] = class_
    attr_str = ''.join(f' {name}="{value}"' for name, value in attrs.items())
    if content:
        return '\n'.join(f'<{name}{attr_str}>{c}</{name}>' for c in content)
    else:
        return f'<{name}{attr_str} />'

@pytest.mark.parametrize('args,kwargs,expected', [
    (('br',), {}, '<br />'),
    (('p', 'hello'), {}, '<p>hello</p>'),
    (('p', 'hello', 'world'), {}, '<p>hello</p>\n<p>world</p>'),
    (('p', 'hello', 'world'), {'class_': 'sidebar'}, '<p class="sidebar">hello</p>\n<p class="sidebar">world</p>'),
    ((), {'content': 'testing', 'name': 'img'}, '<img content="testing" />'),
    ((), {'name': 'img', 'title': 'Sunset Boulevard', 'src': 'sunset.jpg', 'class': 'framed'}, 
        '<img title="Sunset Boulevard" src="sunset.jpg" class="framed" />'),
])
def test_tag(args, kwargs, expected):
    assert tag(*args, **kwargs) == expected