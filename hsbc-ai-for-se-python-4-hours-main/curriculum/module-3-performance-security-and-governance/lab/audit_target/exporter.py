"""Builds the text statement that is sent to a client."""
import logging

log = logging.getLogger(__name__)


def render_statement(account_number: str, rows: list[dict]) -> str:
    log.info("Exporting statement for account %s", account_number)
    lines = [f"Account: {account_number}"]
    for row in rows:
        lines.append(f"{row['id']} {row['amount']} {row['currency']}")
    return "\n".join(lines)
