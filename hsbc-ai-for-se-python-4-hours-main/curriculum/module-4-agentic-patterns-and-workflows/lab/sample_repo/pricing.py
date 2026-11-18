"""Black-Scholes pricing and Greeks calculations."""

import math

from models import Option, OptionType


def _d1(opt: Option) -> float:
    return (
        math.log(opt.spot / opt.strike)
        + (opt.risk_free_rate + 0.5 * opt.volatility ** 2) * opt.time_to_expiry
    ) / (opt.volatility * math.sqrt(opt.time_to_expiry))


def _norm_cdf(x: float) -> float:
    return 0.5 * (1 + math.erf(x / math.sqrt(2)))


def _norm_pdf(x: float) -> float:
    return math.exp(-0.5 * x ** 2) / math.sqrt(2 * math.pi)


def delta(opt: Option) -> float:
    """Per-contract delta, from the perspective of a LONG holder."""
    d1 = _d1(opt)
    if opt.option_type == OptionType.CALL:
        return _norm_cdf(d1)
    return _norm_cdf(d1) - 1


def gamma(opt: Option) -> float:
    """Per-contract gamma (same formula for calls and puts)."""
    d1 = _d1(opt)
    return _norm_pdf(d1) / (opt.spot * opt.volatility * math.sqrt(opt.time_to_expiry))
