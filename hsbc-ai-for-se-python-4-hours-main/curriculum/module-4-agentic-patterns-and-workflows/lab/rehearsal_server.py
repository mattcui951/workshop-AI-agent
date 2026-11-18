"""OFFLINE REHEARSAL (instructors): a scripted stand-in for the Claude API, so you can dry-run the demos with no key and no network.

    python3 rehearsal_server.py agent [port]        # then:  ANTHROPIC_API_KEY=offline ANTHROPIC_BASE_URL=http://127.0.0.1:8089 python3 agent_app.py
    python3 rehearsal_server.py pr-review [port]    # then:  ANTHROPIC_API_KEY=offline ANTHROPIC_BASE_URL=http://127.0.0.1:8089 python3 pr_review_app.py

It is NOT a model: it replays a fixed script. Use it to rehearse the flow and the approval gates, never to judge what Claude would
actually do. (The pr-review script reads the reference fix from ../solutions/portfolio_fixed.py.)
"""
import sys
import threading
from pathlib import Path

from scripted_anthropic import ScriptedAnthropic, final_text, tool_use


def build_script(scenario: str) -> list[str]:
    if scenario == "agent":
        return [
            tool_use("toolu_1", "query_transactions_db",
                     {"sql": "SELECT SUM(amount) AS total FROM transactions WHERE customer_id = 'CUST100' AND status = 'settled'"}),
            tool_use("toolu_2", "get_fx_rate", {"currency_pair": "USD/EUR"}),
            final_text("Settled transactions for CUST100 total 5,000.00 USD (from the ledger). At the current USD/EUR rate of 0.92 "
                       "(from the FX service) that is 4,600.00 EUR."),
        ]
    if scenario == "pr-review":
        fixed = (Path(__file__).resolve().parent.parent / "solutions" / "portfolio_fixed.py").read_text(encoding="utf-8")
        return [
            tool_use("t1", "list_files", {}),
            tool_use("t2", "read_ci_report", {}),
            tool_use("t3", "read_file", {"path": "portfolio.py"}),
            tool_use("t4", "propose_patch", {"path": "portfolio.py", "new_content": fixed,
                                              "explanation": "net_delta and net_gamma ignore Position.side, so short positions are counted as long."}),
            final_text("Root cause: portfolio.net_delta and net_gamma never use Position.side, so every short position is counted as long. "
                       "I proposed a fix in portfolio.py. I did not touch the tests."),
        ]
    raise SystemExit("scenario must be 'agent' or 'pr-review'")


if __name__ == "__main__":
    scenario = sys.argv[1] if len(sys.argv) > 1 else "agent"
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8089
    server = ScriptedAnthropic(build_script(scenario), port)
    print(f"Offline rehearsal server ({scenario}) listening on {server.base_url}  - scripted replies, not a real model")
    threading.Event().wait()
