"""The tools the model may ask for. The model only ever sees a name, a description and an input schema.

Each tool carries its own safety controls: a tool is a new thing to govern, exactly like a new data source.
"""
from __future__ import annotations

import json
import re
import sqlite3
import urllib.request
from typing import Protocol

import safe_sql

MAX_ROWS = 50
CURRENCIES = {"USD", "EUR", "GBP"}


class Tool(Protocol):
    name: str
    description: str
    input_schema: dict

    def execute(self, tool_input: dict) -> str: ...


class QueryTransactionsTool:
    """Read-only SQL. Layers: safe_sql check, a read-only database connection, a row cap. (The human approval is in the agent.)"""

    name = "query_transactions_db"
    description = (
        "Run ONE read-only SQL SELECT against the ledger. Table: transactions(id, customer_id, amount, currency, status) "
        "where status is settled, pending or failed. Returns at most 50 rows."
    )
    input_schema = {"type": "object", "properties": {"sql": {"type": "string", "description": "A single SELECT statement"}},
                    "required": ["sql"]}

    def __init__(self, conn: sqlite3.Connection) -> None:
        self._conn = conn

    def execute(self, tool_input: dict) -> str:
        sql = safe_sql.validate(tool_input.get("sql"))
        cursor = self._conn.execute(sql)
        columns = [d[0] for d in cursor.description]
        rows = [dict(zip(columns, row)) for row in cursor.fetchmany(MAX_ROWS)]
        return json.dumps(rows)


class FxRateTool:
    """Calls the internal FX API. Controls: the pair must match a fixed pattern and use known currencies (so the model
    cannot steer the URL), the base URL is fixed in configuration, GET only, short timeout."""

    name = "get_fx_rate"
    description = "Get the current exchange rate for a currency pair such as USD/EUR. Currencies: USD, EUR, GBP."
    input_schema = {"type": "object", "properties": {"currency_pair": {"type": "string", "description": "e.g. USD/EUR"}},
                    "required": ["currency_pair"]}

    def __init__(self, base_url: str) -> None:
        self._base_url = base_url

    def execute(self, tool_input: dict) -> str:
        pair = str(tool_input.get("currency_pair", "")).upper()
        if not re.fullmatch(r"[A-Z]{3}/[A-Z]{3}", pair) or not set(pair.split("/")) <= CURRENCIES:
            raise ValueError(f"Unsupported currency pair: {pair!r}")
        url = f"{self._base_url}/rates/{pair.replace('/', '-')}"
        with urllib.request.urlopen(url, timeout=5) as response:        # noqa: S310  (fixed http base URL, validated path)
            return response.read().decode()


class ToolRegistry:
    """The allow-list: the agent can use these tools and nothing else."""

    def __init__(self) -> None:
        self._tools: dict[str, Tool] = {}

    def register(self, tool: Tool) -> "ToolRegistry":
        self._tools[tool.name] = tool
        return self

    def find(self, name: str) -> Tool | None:
        return self._tools.get(name)

    def definitions(self) -> list[dict]:
        """The `tools` list sent to the Messages API."""
        return [{"name": t.name, "description": t.description, "input_schema": t.input_schema} for t in self._tools.values()]
