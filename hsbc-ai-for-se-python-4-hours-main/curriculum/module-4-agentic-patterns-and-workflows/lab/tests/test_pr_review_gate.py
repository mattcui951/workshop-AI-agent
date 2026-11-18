"""The human gate. Starts RED: pr_review_app.make_gate() currently approves everything without asking anyone."""
from approval import deny_all
from pr_review_app import SAFE_TOOLS, make_gate


def test_safe_tools_run_without_bothering_the_human():
    gate = make_gate(deny_all)
    for tool in SAFE_TOOLS:
        assert gate(tool, {}) is True


def test_applying_a_patch_needs_the_human_and_a_no_means_no():
    assert make_gate(deny_all)("apply_patch", {"path": "portfolio.py"}) is False


def test_the_human_is_actually_asked_and_their_yes_is_honoured():
    asked = []
    gate = make_gate(lambda action, details: asked.append((action, details)) or True)
    assert gate("apply_patch", {"path": "portfolio.py"}) is True
    assert asked == [("apply_patch", {"path": "portfolio.py"})]


def test_an_unknown_action_is_never_waved_through():
    assert make_gate(deny_all)("delete_everything", {}) is False
