from fractions import Fraction

class ArithmeticProgression:
    def __init__(self, begin, step, end=None):
        self.begin = begin
        self.step = step
        self.end = end
        
    def __iter__(self):
        index = 0
        current = self.begin + index * self.step
        while self.end is None or current < self.end:
            yield current
            index += 1
            current = self.begin + index * self.step


def test_arithmetic_progression():
    assert list(ArithmeticProgression(0, 2, 5)) == [0, 2, 4]
    assert list(ArithmeticProgression(0, Fraction(1, 3), 1)) \
        == [Fraction(0, 1), Fraction(1, 3), Fraction(2, 3)]
