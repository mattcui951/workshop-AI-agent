"""A payment transaction. Amounts are Decimal, never float: Decimal sums are exact."""
from dataclasses import dataclass
from decimal import Decimal


@dataclass(frozen=True)
class Transaction:
    txn_id: str
    customer_id: str
    amount: Decimal
    currency: str
    status: str
