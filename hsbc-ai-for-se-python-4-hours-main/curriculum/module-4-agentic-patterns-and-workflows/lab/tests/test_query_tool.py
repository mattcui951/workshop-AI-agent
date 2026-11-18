"""Needs YOUR safe_sql.validate(), so these stay red until it is written."""
import json

import pytest

from database import build_database
from tools import MAX_ROWS, QueryTransactionsTool


def test_the_query_tool_returns_rows_as_json():
    rows = json.loads(QueryTransactionsTool(build_database()).execute(
        {"sql": "SELECT SUM(amount) AS total FROM transactions WHERE customer_id = 'CUST100' AND status = 'settled'"}))
    assert rows == [{"total": 5000.0}]


def test_the_query_tool_rejects_writes_before_touching_the_database():
    conn = build_database()
    with pytest.raises(ValueError):
        QueryTransactionsTool(conn).execute({"sql": "DELETE FROM transactions"})
    assert conn.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 4


def test_the_query_tool_caps_the_number_of_rows():
    rows = json.loads(QueryTransactionsTool(build_database()).execute(
        {"sql": "SELECT a.id FROM transactions a, transactions b, transactions c"}))      # 4 x 4 x 4 = 64 rows
    assert len(rows) == MAX_ROWS
