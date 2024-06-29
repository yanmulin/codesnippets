def tree(cls: type, level: int = 0):
    yield cls.__name__, level
    for subclass in cls.__subclasses__():
        yield from tree(subclass, level + 1)

def display(cls):
    for cls, level in tree(cls):
        indent = '  ' * level
        print(f'{indent}{cls}')

if __name__ == '__main__':
    display(Exception)