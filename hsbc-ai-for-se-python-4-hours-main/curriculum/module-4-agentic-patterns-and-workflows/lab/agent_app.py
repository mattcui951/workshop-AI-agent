"""Interactive demo of the tool-calling agent.

    python3 agent_app.py ["your question"]          (needs ANTHROPIC_API_KEY; or point ANTHROPIC_BASE_URL at the rehearsal server)

Approve or decline each tool call when asked. AUTO_APPROVE=1 approves everything: for unattended runs only.
"""
import os
import sys

import anthropic

import approval
from agent import ToolAgent
from database import build_database
from fx_service import FxService
from tools import FxRateTool, QueryTransactionsTool, ToolRegistry

SYSTEM = (
    "You are an operations assistant for a payments desk. Use the tools to answer with real data; never guess numbers. "
    "Say clearly which figures came from which tool. If a tool call is declined or fails, explain what you could not do "
    "instead of making something up."
)


def main() -> None:
    question = " ".join(sys.argv[1:]) or "What is the total amount of settled transactions for customer CUST100, in EUR?"
    fx = FxService()                                                    # synthetic internal API on a free local port
    try:
        registry = ToolRegistry().register(QueryTransactionsTool(build_database())).register(FxRateTool(fx.base_url))
        client = anthropic.Anthropic(api_key=os.environ["ANTHROPIC_API_KEY"], base_url=os.environ.get("ANTHROPIC_BASE_URL"))
        agent = ToolAgent(client, registry, approval.from_environment(), max_steps=6)
        print(f"Question: {question}")
        result = agent.run(SYSTEM, question)
        print("\n--- Audit trail ---")
        for call in result.audit:
            print(f"{call.tool:<22} approved={str(call.approved):<5} error={str(call.error):<5} {call.result_preview}")
        print(f"\n--- Answer ({result.stop_reason}, {result.steps} model calls) ---\n{result.final_text}")
    finally:
        fx.stop()


if __name__ == "__main__":
    main()
