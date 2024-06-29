import pytest
from abc import ABC, abstractmethod
from dataclasses import dataclass
from decimal import Decimal
from typing import NamedTuple, Optional, Sequence

class Customer(NamedTuple):
    name: str
    fidelity: int

class LineItem(NamedTuple):
    product: str
    quantity: int
    price: Decimal
    
    def total(self) -> Decimal:
        return self.quantity  * self.price

class Promotion(ABC):
    @abstractmethod
    def discount(self, order: 'Order') -> Decimal:
        pass

@dataclass
class Order:
    customer: Customer
    cart: Sequence[LineItem]
    promotion: Optional[Promotion]

    def total(self) -> Decimal:
        return sum((item.total() for item in self.cart), start=Decimal(0))
    
    def due(self) -> Decimal:
        discount = self.promotion.discount(self) if self.promotion else Decimal(0)
        return self.total() - discount

    def __repr__(self) -> str:
        return f'<Order total: {self.total():.2f} due: {self.due():.2f}>'

class FidelityPromo(Promotion):
    def discount(self, order: 'Order') -> Decimal:
        if order.customer.fidelity >= 1000:
            return order.total() * Decimal('0.05')
        else:
            return Decimal(0)

class BulkItemPromo(Promotion):
    def discount(self, order: 'Order') -> Decimal:
        each_item_discount = (item.total() * Decimal('0.1') 
            for item in order.cart if item.quantity >= 20)
        return sum(each_item_discount, start=Decimal(0))

class LargeOrderPromo(Promotion):
    def discount(self, order: 'Order') -> Decimal:
        distinct_items = set((item.product for item in order.cart))
        if len(distinct_items) >= 10:
            return order.total() * Decimal('0.07')
        else:
            return Decimal(0)

@pytest.mark.parametrize("fidelity,items,promotion,expected", [
    (0, (LineItem('a', 5, 10), LineItem('b', 3, 2)), None, '56'),
    (999, (LineItem('a', 5, 10), LineItem('b', 3, 2)), FidelityPromo(), '56'),
    (1000, (LineItem('a', 5, 10), LineItem('b', 3, 2)), FidelityPromo(), '53.2'),
    (0, (LineItem('a', 20, 5), LineItem('b', 3, 2)), BulkItemPromo(), '96'),
    (0, (LineItem('a', 3, 2), LineItem('b', 3, 2), LineItem('c', 3, 2), 
        LineItem('d', 3, 2), LineItem('e', 3, 2), LineItem('f', 3, 2), 
        LineItem('g', 3, 2), LineItem('h', 3, 2), LineItem('i', 3, 2), 
        LineItem('j', 3, 2)), LargeOrderPromo(), '55.8'),
])
def test_promotion(fidelity, items, promotion, expected):
    assert Order(
        customer=Customer(name='xxx', fidelity=fidelity),
        cart=items,
        promotion=promotion
    ).due() == Decimal(expected)
