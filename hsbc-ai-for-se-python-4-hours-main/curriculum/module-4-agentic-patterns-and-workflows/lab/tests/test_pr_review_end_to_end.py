"""The whole PR-review flow with a scripted model: a final check once your agent loop AND your gate are done.
The model 'proposes' the reference fix (read from ../solutions); nothing changes on disk until a human approves."""
import shutil
from pathlib import Path

import anthropic
import pytest

import patch_applier
import repo_tools
from agent import ToolAgent
from approval import deny_all
from pr_review_app import make_gate
from scripted_anthropic import ScriptedAnthropic, final_text, tool_use
from tools import ToolRegistry

LAB = Path(__file__).resolve().parent.parent
FIXED = LAB.parent / "solutions" / "portfolio_fixed.py"


@pytest.fixture
def repo(tmp_path):
    dest = tmp_path / "repo"
    shutil.copytree(LAB / "sample_repo", dest, ignore=shutil.ignore_patterns("__pycache__", ".pytest_cache"))
    return dest


def run_agent(repo, human):
    proposals = []
    registry = (ToolRegistry().register(repo_tools.list_files(repo)).register(repo_tools.read_file(repo))
                .register(repo_tools.read_ci_report(repo)).register(repo_tools.propose_patch(repo, proposals)))
    api = ScriptedAnthropic([
        tool_use("t1", "list_files", {}), tool_use("t2", "read_ci_report", {}),
        tool_use("t3", "read_file", {"path": "portfolio.py"}),
        tool_use("t4", "propose_patch", {"path": "portfolio.py", "new_content": FIXED.read_text(), "explanation": "short positions counted as long"}),
        final_text("Root cause found: Position.side is ignored."),
    ])
    try:
        gate = make_gate(human)
        client = anthropic.Anthropic(api_key="test-key", base_url=api.base_url, max_retries=0)
        result = ToolAgent(client, registry, gate, model="test-model", max_steps=10).run("sys", "CI failed overnight")
        return result, proposals, gate, api
    finally:
        api.close()


@pytest.mark.skipif(not FIXED.exists(), reason="solutions/ is not present in this copy of the repo")
def test_the_agent_proposes_a_fix_and_nothing_changes_until_a_human_approves(repo):
    original = (repo / "portfolio.py").read_text()
    result, proposals, gate, api = run_agent(repo, deny_all)
    assert result.steps == 5 and len(proposals) == 1
    assert "Position.side" in result.final_text
    assert (repo / "portfolio.py").read_text() == original                       # a proposal alone changes nothing
    assert "def net_delta" in str(api.requests)                                      # the model really was shown the code

    assert patch_applier.apply_if_approved(repo, proposals[0], gate) is False      # the human says no through the SAME gate
    assert (repo / "portfolio.py").read_text() == original
    assert not (repo / "portfolio.py.bak").exists()

    approved = make_gate(lambda a, d: True)
    assert patch_applier.apply_if_approved(repo, proposals[0], approved) is True
    assert (repo / "portfolio.py").read_text() == proposals[0].new_content
    assert (repo / "portfolio.py.bak").read_text() == original
