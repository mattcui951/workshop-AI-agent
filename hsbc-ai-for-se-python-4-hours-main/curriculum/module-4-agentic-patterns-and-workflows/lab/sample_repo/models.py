"""Domain models for the derivatives position risk calculator.

This is a simplified version of a trade book: a set of options
positions whose aggregate risk (delta, gamma) gets reported to the
desk each morning.
"""

from dataclasses import dataclass
from enum import Enum


class OptionType(Enum):
    CALL = "call"
    PUT = "put"


class Side(Enum):
    LONG = "long"
    SHORT = "short"


@dataclass
class Option:
    underlying: str
    option_type: OptionType
    strike: float
    spot: float
    time_to_expiry: float  # in years
    volatility: float
    risk_free_rate: float = 0.03


@dataclass
class Position:
    option: Option
    side: Side
    quantity: int  # number of contracts, always positive
