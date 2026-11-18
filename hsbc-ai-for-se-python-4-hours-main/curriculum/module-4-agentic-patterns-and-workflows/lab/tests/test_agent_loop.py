"""The agent loop, tested against a scripted stand-in for the API. These prove the MECHANICS (approvals, safety controls),
never what a real model will decide."""
import anthropic
import pytest

from agent import ToolAgent
from approval import deny_all
from database import build_database
from fx_service import FxService
from scripted_anthropic import ScriptedAnthropic, final_text, tool_use
from tools import FxRateTool, QueryTransactionsTool, ToolRegistry

SYSTEM = "test system prompt"


@pytest.fixture
def world():
    fx = FxService()
    conn = build_database()
    registry = ToolRegistry().register(QueryTransactionsTool(conn)).register(FxRateTool(fx.base_url))
    yield conn, registry
    fx.stop()


def agent_for(api, registry, gate, max_steps=6):
    client = anthropic.Anthropic(api_key="test-key", base_url=api.base_url, max_retries=0)
    return ToolAgent(client, registry, gate, model="test-model", max_steps=max_steps)


def last_tool_result(request):
    return request["messages"][-1]["content"][0]


def test_approved_tools_run_and_their_results_go_back_to_the_model(world):
    conn, registry = world
    asked = []
    api = ScriptedAnthropic([
        tool_use("toolu_1", "query_transactions_db", {"sql": "SELECT SUM(amount) AS total FROM transactions WHERE customer_id = 'CUST100' AND status = 'settled'"}),
        tool_use("toolu_2", "get_fx_rate", {"currency_pair": "USD/EUR"}),
        final_text("5,000 USD is 4,600 EUR."),
    ])
    try:
        result = agent_for(api, registry, lambda action, details: asked.append(action) or True).run(SYSTEM, "How much, in EUR?")
    finally:
        api.close()
    assert (result.stop_reason, result.steps, result.final_text) == ("end_turn", 3, "5,000 USD is 4,600 EUR.")
    assert asked == ["query_transactions_db", "get_fx_rate"]                      # a human saw every call
    assert len(result.audit) == 2 and all(c.approved and not c.error for c in result.audit)
    second = api.requests[1]                                                       # the 1st result went back, linked by id
    assert len(second["messages"]) == 3
    assert last_tool_result(second)["type"] == "tool_result" and last_tool_result(second)["tool_use_id"] == "toolu_1"
    assert "5000" in last_tool_result(second)["content"]
    assert [t["name"] for t in second["tools"]] == ["query_transactions_db", "get_fx_rate"]


def test_a_declined_call_never_runs_and_the_model_is_told_why(world):
    conn, registry = world
    api = ScriptedAnthropic([tool_use("toolu_1", "get_fx_rate", {"currency_pair": "USD/EUR"}), final_text("I could not get the rate.")])
    try:
        result = agent_for(api, registry, deny_all).run(SYSTEM, "rate?")
    finally:
        api.close()
    call = result.audit[0]
    assert (call.approved, call.error, call.result_preview) == (False, True, "declined by human reviewer")
    sent = last_tool_result(api.requests[1])
    assert sent["is_error"] is True and "declined" in sent["content"]


def test_even_if_a_human_approves_a_write_the_sql_tool_still_blocks_it(world):
    conn, registry = world
    api = ScriptedAnthropic([tool_use("toolu_1", "query_transactions_db", {"sql": "DELETE FROM transactions"}), final_text("blocked")])
    try:
        result = agent_for(api, registry, lambda a, d: True).run(SYSTEM, "clean up")
    finally:
        api.close()
    assert result.audit[0].approved and result.audit[0].error
    assert last_tool_result(api.requests[1])["is_error"] is True
    assert conn.execute("SELECT COUNT(*) FROM transactions").fetchone()[0] == 4      # data intact


def test_unknown_tools_are_refused(world):
    conn, registry = world
    api = ScriptedAnthropic([tool_use("toolu_1", "send_email", {"to": "x@example.com"}), final_text("done")])
    try:
        result = agent_for(api, registry, lambda a, d: True).run(SYSTEM, "email")
    finally:
        api.close()
    assert result.audit[0].result_preview == "unknown tool" and not result.audit[0].approved
    assert last_tool_result(api.requests[1])["is_error"] is True


def test_a_tool_that_crashes_is_reported_to_the_model_not_raised(world):
    conn, registry = world
    api = ScriptedAnthropic([tool_use("toolu_1", "get_fx_rate", {"currency_pair": "USD/JPY"}), final_text("unsupported")])
    try:
        result = agent_for(api, registry, lambda a, d: True).run(SYSTEM, "yen?")
    finally:
        api.close()
    assert result.audit[0].approved and result.audit[0].error
    assert "Unsupported currency pair" in last_tool_result(api.requests[1])["content"]


def test_long_tool_output_is_truncated_before_it_goes_back(world):
    conn, registry = world

    class Firehose:
        name, description, input_schema = "firehose", "lots of text", {"type": "object", "properties": {}}

        def execute(self, tool_input):
            return "x" * 10_000

    registry.register(Firehose())
    api = ScriptedAnthropic([tool_use("toolu_1", "firehose", {}), final_text("ok")])
    try:
        agent_for(api, registry, lambda a, d: True).run(SYSTEM, "go")
    finally:
        api.close()
    content = last_tool_result(api.requests[1])["content"]
    assert len(content) < 4100 and content.endswith("[truncated]")


def test_the_loop_stops_at_the_step_limit(world):
    conn, registry = world
    api = ScriptedAnthropic([tool_use("toolu_x", "get_fx_rate", {"currency_pair": "USD/GBP"})])       # the last reply repeats forever
    try:
        result = agent_for(api, registry, lambda a, d: True, max_steps=3).run(SYSTEM, "loop")
    finally:
        api.close()
    assert result.stop_reason == "max_steps" and len(api.requests) == 3 and "limit" in result.final_text
