"""An embedded, in-memory SQLite ledger with synthetic data: nothing to install or provision.

After seeding, the connection is switched to READ-ONLY (PRAGMA query_only). Even if a bad query slipped past
safe_sql, the database itself refuses to write. That is least privilege, and it is the real control: string
checks are only a backstop. (SQLite-specific. A real service uses a read-only database account.)
"""
import sqlite3


def build_database() -> sqlite3.Connection:
    conn = sqlite3.connect(":memory:", check_same_thread=False)
    conn.execute("""CREATE TABLE transactions (
        id TEXT PRIMARY KEY, customer_id TEXT, amount REAL, currency TEXT, status TEXT)""")
    conn.executemany("INSERT INTO transactions VALUES (?, ?, ?, ?, ?)", [
        ("TXN001", "CUST100", 5000.00, "USD", "settled"),
        ("TXN002", "CUST101", 12000.50, "EUR", "pending"),
        ("TXN003", "CUST100", 750.25, "USD", "failed"),
        ("TXN004", "CUST102", 3200.00, "GBP", "settled"),
    ])
    conn.commit()
    conn.execute("PRAGMA query_only = ON")          # from here on, this connection can only read
    return conn
