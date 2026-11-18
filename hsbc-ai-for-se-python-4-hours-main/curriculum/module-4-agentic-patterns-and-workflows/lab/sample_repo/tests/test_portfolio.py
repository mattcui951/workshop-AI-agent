import pytest

from models import Option, OptionType, Position, Side
from portfolio import net_delta, net_gamma

CALL = Option(underlying="XYZ", option_type=OptionType.CALL, strike=100, spot=105, time_to_expiry=0.5, volatility=0.25)
PUT = Option(underlying="XYZ", option_type=OptionType.PUT, strike=100, spot=105, time_to_expiry=0.5, volatility=0.25)

BOOK = [
    Position(option=CALL, side=Side.LONG, quantity=100),
    Position(option=PUT, side=Side.SHORT, quantity=150),
    Position(option=CALL, side=Side.SHORT, quantity=50),
]


def test_an_empty_book_has_no_exposure():
    assert net_delta([]) == 0 and net_gamma([]) == 0


def test_a_long_only_book_adds_up():
    long_only = [Position(option=CALL, side=Side.LONG, quantity=10), Position(option=CALL, side=Side.LONG, quantity=5)]
    assert net_delta(long_only) == pytest.approx(net_delta([Position(option=CALL, side=Side.LONG, quantity=15)]))


def test_a_short_position_subtracts_delta():
    long_call = net_delta([Position(option=CALL, side=Side.LONG, quantity=10)])
    short_call = net_delta([Position(option=CALL, side=Side.SHORT, quantity=10)])
    assert short_call == pytest.approx(-long_call)


def test_net_delta_for_the_sample_book():
    assert net_delta(BOOK) == pytest.approx(82.66, abs=0.01)


def test_net_gamma_for_the_sample_book():
    assert net_gamma(BOOK) == pytest.approx(-1.94, abs=0.01)
