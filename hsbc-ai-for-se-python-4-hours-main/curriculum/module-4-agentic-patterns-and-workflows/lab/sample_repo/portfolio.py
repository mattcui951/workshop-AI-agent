"""Aggregates position-level Greeks into portfolio-level risk figures."""

from models import Position
from pricing import delta, gamma


def net_delta(positions: list[Position]) -> float:
    """Total delta exposure across the book, in shares-equivalent."""
    total = 0.0
    for pos in positions:
        total += delta(pos.option) * pos.quantity
    return total


def net_gamma(positions: list[Position]) -> float:
    """Total gamma exposure across the book."""
    total = 0.0
    for pos in positions:
        total += gamma(pos.option) * pos.quantity
    return total
