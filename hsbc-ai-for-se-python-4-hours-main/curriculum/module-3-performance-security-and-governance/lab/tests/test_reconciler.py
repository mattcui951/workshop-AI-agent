"""Hand-written behaviour checks: the safety net that must stay green while you refactor."""
from decimal import Decimal

from reconciler import count_by_status, find_duplicate_ids, total_by_currency
from transactions import Transaction


def t(txn_id, amount="1", currency="USD", status="NEW"):
    return Transaction(txn_id, "CUST1", Decimal(amount), currency, status)


def test_duplicates_are_listed_in_order_of_first_occurrence():
    # A B B A: a version that reports ids in order of their SECOND occurrence answers ['B', 'A'], which is wrong.
    trades = [t("A"), t("B"), t("B"), t("A"), t("C")]
    assert find_duplicate_ids(trades) == ["A", "B"]


def test_an_id_seen_three_times_is_reported_once():
    assert find_duplicate_ids([t("A"), t("A"), t("A")]) == ["A"]


def test_no_duplicates_and_empty_input():
    assert find_duplicate_ids([t("A"), t("B")]) == []
    assert find_duplicate_ids([]) == []
    assert total_by_currency([]) == {}
    assert count_by_status([]) == {}


def test_totals_per_currency_are_exact():
    txns = [t("1", "0.10", "USD"), t("2", "0.20", "USD"), t("3", "5.50", "EUR")]
    assert total_by_currency(txns) == {"USD": Decimal("0.30"), "EUR": Decimal("5.50")}


def test_counts_per_status():
    assert count_by_status([t("1", status="SETTLED"), t("2", status="SETTLED"), t("3", status="FAILED")]) == {"SETTLED": 2, "FAILED": 1}
