import pytest

class Quantity:
    def __init__(self, storage_name):
        self.storage_name = storage_name
    
    def __get__(self, instance, owner):
        return instance.__dict__[self.storage_name]
    
    def __set__(self, instance, value):
        if value > 0:
            instance.__dict__[self.storage_name] = value
        else:
            raise ValueError('value must be > 0')

class LineItem:
    
    weight = Quantity('weight')
    price = Quantity('price')
    
    def __init__(self, description, weight, price):
        self.description = description
        self.weight = weight
        self.price = price
        
    def subtotal(self):
        return self.weight * self.price

def test_weight():
    item = LineItem('desc', 5, 5)
    item.weight = 10
    assert item.weight == 10
    setattr(item, 'weight', 1)
    assert getattr(item, 'weight') == 1
    
    with pytest.raises(ValueError):
        LineItem('desc', -5, 10)
    
    with pytest.raises(ValueError):
        item.weight = 0

def test_price():
    item = LineItem('desc', 5, 5)
    item.price = 10
    assert item.price == 10
    
    with pytest.raises(ValueError):
        LineItem('desc', 5, -10)
    
    with pytest.raises(ValueError):
        item.price = 0
