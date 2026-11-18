"""PR-review / triage agent:  python3 pr_review_app.py [path-to-repo]

YOUR JOB (You Do): the human approval gate is broken. Read make_gate(), find the TODO, fix it,
then make   pytest -q tests/test_pr_review_gate.py   go green.

The agent reads the CI report and the code and PROPOSES a fix. A human approves before anything is written.
"""
import os
import sys
from pathlib import Path

import anthropic

import approval
import patch_applier
import repo_tools
from agent import ToolAgent
from tools import ToolRegistry

SYSTEM = (
    "You are a code review and triage assistant for a Python service. You can list files, read source files and read the "
    "CI report. You CANNOT change anything. When you have found the root cause, call propose_patch once with the complete "
    "corrected file and a short explanation. Prefer fixing the root cause over hiding the symptom, and say what you are unsure about."
)

# Tier approvals by risk: reading and proposing change nothing, so they run freely.
# APPLYING a patch is where the human decides.
SAFE_TOOLS = {"list_files", "read_file", "read_ci_report", "propose_patch"}


def make_gate(human):
    """The gate the agent and the patch applier share.

    TODO (lab): this approves EVERY action without asking anyone, so the human is never consulted: not even
    before a patch is written to disk. That is not a human gate. Fix it so that:
      - the SAFE_TOOLS run freely (reading and proposing change nothing)
      - every other action, including "apply_patch", is decided by `human(action, details)`
    """
    return lambda action, details: True


def main() -> None:
    repo = Path(sys.argv[1] if len(sys.argv) > 1 else "sample_repo")
    human = approval.from_environment()
    gate = make_gate(human)

    proposals: list[repo_tools.PendingPatch] = []
    registry = (ToolRegistry().register(repo_tools.list_files(repo)).register(repo_tools.read_file(repo))
                .register(repo_tools.read_ci_report(repo)).register(repo_tools.propose_patch(repo, proposals)))
    client = anthropic.Anthropic(api_key=os.environ["ANTHROPIC_API_KEY"], base_url=os.environ.get("ANTHROPIC_BASE_URL"))
    result = ToolAgent(client, registry, gate, max_steps=10).run(
        SYSTEM, "The nightly CI run failed. Find the root cause in this repository and propose a fix.")

    print(f"--- Agent's findings ---\n{result.final_text}")
    if not proposals:
        print("\nNo patch was proposed.")
        return
    patch = proposals[-1]
    print(f"\n--- Proposed change (NOT applied yet) ---\n{patch_applier.diff(repo, patch)}")
    applied = patch_applier.apply_if_approved(repo, patch, gate)
    print("\nApplied. Original saved as a .bak file. Now run the tests yourself." if applied
          else "\nNot applied. The repository is unchanged.")


if __name__ == "__main__":
    main()
