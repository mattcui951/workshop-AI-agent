"""FROZEN copy of the original implementation: the oracle. Do not edit.
The tests compare your refactored reconciler.py against this file."""
from decimal import Decimal

from transactions import Transaction

from transactions import Transaction


def find_duplicate_ids(transactions: list[Transaction]) -> list[str]:
    """Ids that appear more than once, each listed once, in the order their FIRST occurrence appears."""
    duplicates = []
    for i in range(len(transactions)):
        for j in range(i + 1, len(transactions)):
            if transactions[i].txn_id == transactions[j].txn_id:
                if transactions[i].txn_id not in duplicates:
                    duplicates.append(transactions[i].txn_id)
    return duplicates


def total_by_currency(transactions: list[Transaction]) -> dict[str, Decimal]:
    """Total amount per currency."""
    totals = {}
    for t in transactions:
        if t.currency in totals:
            totals[t.currency] = totals[t.currency] + t.amount
        else:
            totals[t.currency] = t.amount
    return totals


def count_by_status(transactions: list[Transaction]) -> dict[str, int]:
    """Number of transactions per status."""
    counts = {}
    for t in transactions:
        if t.status in counts:
            counts[t.status] = counts[t.status] + 1
        else:
            counts[t.status] = 1
    return counts
