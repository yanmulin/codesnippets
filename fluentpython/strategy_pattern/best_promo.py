from functional import Order, Customer, LineItem
import pytest
from decimal import Decimal
from typing import Callable

Promotion = Callable[[Order], Decimal]

all_promos: set[Promotion] = set()

def promotion(func: Promotion):
    all_promos.add(func)
    return func

def best_promo(order: Order) -> Decimal:
    return max(promo(order) for promo in all_promos)

@promotion
def fidelity_promo(order: Order) -> Decimal:
    from functional import fidelity_promo
    return fidelity_promo(order)

@promotion
def bulk_item_promo(order: Order) -> Decimal:
    from functional import bulk_item_promo
    return bulk_item_promo(order)

@promotion
def large_order_promo(order: Order) -> Decimal:
    from functional import large_order_promo
    return large_order_promo(order)

@pytest.mark.parametrize("fidelity,items,promotion,expected", [
    (0, (LineItem('a', 5, 10), LineItem('b', 3, 2)), best_promo, '56'),
    (1000, (LineItem('a', 5, 10), LineItem('b', 3, 2)), best_promo, '53.2'),
    (0, (LineItem('a', 20, 5), LineItem('b', 3, 2)), best_promo, '96'),
    (0, (LineItem('a', 3, 2), LineItem('b', 3, 2), LineItem('c', 3, 2), 
        LineItem('d', 3, 2), LineItem('e', 3, 2), LineItem('f', 3, 2), 
        LineItem('g', 3, 2), LineItem('h', 3, 2), LineItem('i', 3, 2), 
        LineItem('j', 3, 2)), best_promo, '55.8'),
])
def test_promotion(fidelity, items, promotion, expected):
    assert Order(
        customer=Customer(name='xxx', fidelity=fidelity),
        cart=items,
        promotion=promotion
    ).due() == Decimal(expected)
