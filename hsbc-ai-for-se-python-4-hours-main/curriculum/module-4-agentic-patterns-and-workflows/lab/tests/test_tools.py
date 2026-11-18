"""Given tests: the tools and their built-in controls. They pass from the start (they use the finished tools.py and database.py)."""
import json
import sqlite3

import pytest

from database import build_database
from fx_service import FxService
from tools import FxRateTool

def test_the_database_itself_refuses_writes():
    conn = build_database()
    with pytest.raises(sqlite3.OperationalError, match="readonly"):
        conn.execute("DELETE FROM transactions WHERE id = 'TXN001'")              # no safe_sql in the way: the database says no
    assert conn.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 4


def test_the_fx_tool_calls_the_internal_api():
    fx = FxService()
    try:
        data = json.loads(FxRateTool(fx.base_url).execute({"currency_pair": "usd/eur"}))
    finally:
        fx.stop()
    assert data == {"pair": "USD/EUR", "rate": 0.92}


@pytest.mark.parametrize("pair", ["../admin", "USD/JPY", "USDEUR", "USD/EUR/GBP", "", "usd-eur"])
def test_the_fx_tool_only_accepts_known_currency_pairs(pair):
    with pytest.raises(ValueError):
        FxRateTool("http://127.0.0.1:1").execute({"currency_pair": pair})
