"""Security regression tests. They describe how the code SHOULD behave, so on the vulnerable starting code
most of them FAIL. Fix the code until they pass, then keep them forever.   Run:  pytest -q tests/test_security.py"""
import logging
import pickle
import sqlite3

import pytest

import helpers
from audit_target import auth, config, exporter, repository, session_cache


@pytest.fixture
def db():
    conn = sqlite3.connect(":memory:")
    conn.execute("CREATE TABLE transactions (id TEXT, customer_id TEXT, amount REAL, currency TEXT, status TEXT)")
    conn.executemany("INSERT INTO transactions VALUES (?,?,?,?,?)", [
        ("TXN001", "CUST100", 5000.0, "USD", "settled"), ("TXN002", "CUST101", 12000.5, "EUR", "pending"),
        ("TXN003", "CUST100", 750.25, "USD", "failed"), ("TXN004", "CUST102", 3200.0, "GBP", "settled")])
    return conn


# ---- 1. SQL injection ----
def test_a_normal_lookup_still_works(db):
    assert len(repository.get_transactions_by_customer(db, "CUST100")) == 2


def test_an_injection_payload_returns_nothing(db):
    # A classic payload. A safe repository treats it as a customer id that simply does not exist.
    assert repository.get_transactions_by_customer(db, "x' OR '1'='1") == []


def test_a_stacked_payload_is_harmless_and_does_not_crash(db):
    assert repository.get_transactions_by_customer(db, "x'; DELETE FROM transactions; --") == []
    assert db.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 4


# ---- 2. Hard-coded credentials ----
def test_secrets_come_from_the_environment_not_from_source():
    env = {"DB_PASSWORD": "from-env", "PRICING_API_KEY": "key-from-env"}
    assert config.db_password(env) == "from-env"
    assert config.pricing_api_key(env) == "key-from-env"


def test_missing_secrets_fail_fast_instead_of_falling_back_to_a_default():
    with pytest.raises(RuntimeError):
        config.db_password({})
    with pytest.raises(RuntimeError):
        config.pricing_api_key({})


# ---- 3. Unsafe deserialisation ----
def test_a_session_round_trips():
    assert session_cache.load(session_cache.save({"user": "jo", "roles": ["ops"]})) == {"user": "jo", "roles": ["ops"]}


def test_loading_a_malicious_payload_never_runs_its_code():
    helpers.EXECUTED.clear()
    payload = pickle.dumps(helpers.Evil())
    with pytest.raises(ValueError):                 # a safe loader rejects it instead of executing it
        session_cache.load(payload)
    assert helpers.EXECUTED == [], "the payload's code ran: untrusted data must never reach pickle.loads"


# ---- 4. Weak password hashing ----
def test_the_same_password_hashes_differently_each_time():
    assert auth.hash_password("correct horse") != auth.hash_password("correct horse")        # salted


def test_a_correct_password_verifies_and_a_wrong_one_does_not():
    stored = auth.hash_password("correct horse")
    assert auth.verify_password("correct horse", stored) is True
    assert auth.verify_password("wrong horse", stored) is False


# ---- 5. Sensitive data in output and logs ----
def test_the_statement_masks_the_account_number():
    out = exporter.render_statement("12345678901234", [{"id": "TXN001", "amount": 5000.0, "currency": "USD"}])
    assert "12345678901234" not in out and "1234" in out
    assert "TXN001 5000.0 USD" in out


def test_logs_never_contain_the_full_account_number(caplog):
    with caplog.at_level(logging.INFO):
        exporter.render_statement("12345678901234", [])
    assert caplog.records, "expected the exporter to log something"
    assert all("12345678901234" not in r.getMessage() for r in caplog.records)
