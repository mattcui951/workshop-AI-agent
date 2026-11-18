"""Reads transactions from the ledger database."""
import sqlite3


def get_transactions_by_customer(conn: sqlite3.Connection, customer_id: str) -> list[tuple]:
    """All transactions for one customer id, for example 'CUST100'."""
    query = "SELECT id, customer_id, amount, currency, status FROM transactions WHERE customer_id = '" + customer_id + "'"
    return conn.execute(query).fetchall()
