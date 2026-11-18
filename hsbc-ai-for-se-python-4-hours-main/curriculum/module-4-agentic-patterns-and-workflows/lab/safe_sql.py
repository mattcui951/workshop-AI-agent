# PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
"""First line of defence for the SQL tool: accept exactly one plain SELECT statement.

This is a backstop, not the real control. String checks can be bypassed in ways nobody has thought of yet;
the real control is the read-only database connection (see database.py). Keep both.
It is deliberately conservative: it may reject a harmless query that merely contains a forbidden word.
"""
import re

MAX_LENGTH = 2000

_STARTS_WITH_SELECT = re.compile(r"^select\b", re.IGNORECASE)
_FORBIDDEN = re.compile(
    r"\b(insert|update|delete|drop|alter|create|truncate|replace|attach|detach|pragma|vacuum|reindex|load_extension"
    r"|sqlite_master|sqlite_schema|sqlite_temp_master)\b",
    re.IGNORECASE,
)


def validate(sql) -> str:
    """Return the query to run (a trailing semicolon removed) or raise ValueError."""
    if sql is None or not str(sql).strip():
        raise ValueError("SQL is empty")
    s = str(sql).strip()
    if s.endswith(";"):
        s = s[:-1].rstrip()
    if len(s) > MAX_LENGTH:
        raise ValueError("SQL is too long")
    if not _STARTS_WITH_SELECT.match(s):
        raise ValueError("Only SELECT statements are allowed")
    if ";" in s:
        raise ValueError("Only one statement is allowed")
    if "--" in s or "/*" in s:
        raise ValueError("SQL comments are not allowed")
    if _FORBIDDEN.search(s):
        raise ValueError("Query contains a forbidden keyword")
    return s
