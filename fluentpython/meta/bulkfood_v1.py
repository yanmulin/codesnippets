import pytest

class LineItem:
    def __init__(self, description, weight, price):
        self.description = description
        self.weight = weight
        self.price = price
        
    def subtotal(self):
        return self.weight * self.price
    
    @property
    def weight(self):
        return self.__weight
    
    @weight.setter
    def weight(self, value):
        if value > 0:
            self.__weight = value
        else:
            raise ValueError('value must be > 0')

def test_weight():
    item = LineItem('desc', 5, 5)
    item.weight = 10
    assert item.weight == 10
    
    with pytest.raises(ValueError):
        LineItem('desc', -5, 10)
    
    with pytest.raises(ValueError):
        item.weight = 0
