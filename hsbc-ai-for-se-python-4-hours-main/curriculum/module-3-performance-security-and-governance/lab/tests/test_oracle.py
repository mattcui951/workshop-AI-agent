"""Compares your refactored reconciler.py with the frozen original on lots of random data."""
import random
from decimal import Decimal

import legacy_reconciler as legacy
import reconciler as yours
from transactions import Transaction


def random_transactions(n: int, id_range: int, seed: int) -> list[Transaction]:
    rnd = random.Random(seed)
    currencies, statuses = ["USD", "EUR", "GBP"], ["NEW", "PENDING", "SETTLED", "FAILED"]
    return [
        Transaction(f"T-{rnd.randrange(id_range)}", f"C-{rnd.randrange(5)}",
                    Decimal(rnd.randrange(1, 10_000_000)) / 100, rnd.choice(currencies), rnd.choice(statuses))
        for _ in range(n)
    ]


def test_same_duplicates_in_the_same_order():
    for seed in range(1, 21):
        data = random_transactions(400, 300, seed)
        assert yours.find_duplicate_ids(data) == legacy.find_duplicate_ids(data), f"seed {seed}"


def test_same_totals_per_currency():
    data = random_transactions(3000, 100_000, 7)
    assert yours.total_by_currency(data) == legacy.total_by_currency(data)     # Decimal: exact, no tolerance needed


def test_same_counts_per_status():
    data = random_transactions(3000, 100_000, 9)
    assert yours.count_by_status(data) == legacy.count_by_status(data)
