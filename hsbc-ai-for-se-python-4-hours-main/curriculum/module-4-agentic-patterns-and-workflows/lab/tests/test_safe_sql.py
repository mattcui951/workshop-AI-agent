import pytest

import safe_sql


@pytest.mark.parametrize("sql, expected", [
    ("select * from transactions", "select * from transactions"),
    ("  SELECT id FROM transactions;  ", "SELECT id FROM transactions"),
    ("SELECT customer_id, SUM(amount) FROM transactions GROUP BY customer_id", "SELECT customer_id, SUM(amount) FROM transactions GROUP BY customer_id"),
    ("SELECT id, updated_at FROM transactions", "SELECT id, updated_at FROM transactions"),       # 'updated_at' is not the word 'update'
])
def test_accepts_a_single_plain_select(sql, expected):
    assert safe_sql.validate(sql) == expected


@pytest.mark.parametrize("sql", [
    "DELETE FROM transactions",
    "delete from transactions where 1=1",
    "UPDATE transactions SET status='settled'",
    "INSERT INTO transactions VALUES (1)",
    "DROP TABLE transactions",
    "SELECT 1; DROP TABLE transactions",
    "SELECT * FROM transactions; SELECT * FROM transactions",
    "SELECT * FROM transactions -- sneaky",
    "SELECT /* hi */ * FROM transactions",
    "WITH x AS (SELECT 1) DELETE FROM transactions",
    "REPLACE INTO transactions VALUES (1)",
    "ATTACH DATABASE 'x.db' AS x",
    "PRAGMA query_only = OFF",
    "SELECT name FROM sqlite_master",
    "SELECT load_extension('x')",
    "",
    "   ",
])
def test_rejects_everything_else(sql):
    with pytest.raises(ValueError):
        safe_sql.validate(sql)


def test_rejects_none_and_very_long_queries():
    with pytest.raises(ValueError):
        safe_sql.validate(None)
    with pytest.raises(ValueError):
        safe_sql.validate("SELECT " + "1," * 2000 + "1")
