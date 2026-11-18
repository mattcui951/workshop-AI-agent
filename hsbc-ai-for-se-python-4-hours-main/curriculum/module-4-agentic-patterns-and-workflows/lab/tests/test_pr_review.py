"""Given tests: the PR-review tools. They pass from the start: the agent can read and PROPOSE, and only the applier writes."""
import shutil
from pathlib import Path

import pytest

import patch_applier
import repo_tools
from approval import deny_all
from path_guard import resolve_inside
from repo_tools import PendingPatch

SAMPLE = Path(__file__).resolve().parent.parent / "sample_repo"


@pytest.fixture
def repo(tmp_path):
    dest = tmp_path / "repo"
    shutil.copytree(SAMPLE, dest, ignore=shutil.ignore_patterns("__pycache__", ".pytest_cache"))
    return dest


def test_the_path_guard_allows_paths_inside_the_repo(repo):
    assert resolve_inside(repo, "portfolio.py").is_relative_to(repo.resolve())


@pytest.mark.parametrize("bad", ["../secrets.txt", "tests/../../outside.txt", "/etc/passwd", "", "a\0b"])
def test_the_path_guard_blocks_escapes(repo, bad):
    with pytest.raises(ValueError):
        resolve_inside(repo, bad)


def test_the_path_guard_blocks_symlink_escapes(repo, tmp_path):
    outside = tmp_path / "outside.txt"
    outside.write_text("secret")
    try:
        (repo / "link.py").symlink_to(outside)
    except OSError:
        pytest.skip("filesystem without symlinks")
    with pytest.raises(ValueError):
        resolve_inside(repo, "link.py")


def test_list_and_read_tools_stay_in_their_lane(repo):
    listing = repo_tools.list_files(repo).execute({})
    assert "portfolio.py" in listing and "ci-report.txt" in listing and "tests/test_portfolio.py" in listing
    assert "net_delta" in repo_tools.read_file(repo).execute({"path": "portfolio.py"})
    assert "FAILED" in repo_tools.read_ci_report(repo).execute({})
    with pytest.raises(ValueError):
        repo_tools.read_file(repo).execute({"path": "ci-report.txt"})              # not source
    with pytest.raises(ValueError):
        repo_tools.read_file(repo).execute({"path": "../../etc/passwd"})


def test_propose_patch_records_but_never_writes(repo):
    before = (repo / "portfolio.py").read_text()
    sink: list[PendingPatch] = []
    message = repo_tools.propose_patch(repo, sink).execute(
        {"path": "portfolio.py", "new_content": "x = 1\n", "explanation": "e"})
    assert "NOT been applied" in message and len(sink) == 1
    assert (repo / "portfolio.py").read_text() == before                              # untouched
    tool = repo_tools.propose_patch(repo, sink)
    for bad in [{"path": "tests/test_portfolio.py", "new_content": "x", "explanation": "e"},     # tests are out of bounds
                {"path": "ci-report.txt", "new_content": "x", "explanation": "e"},
                {"path": "missing.py", "new_content": "x", "explanation": "e"}]:
        with pytest.raises(ValueError):
            tool.execute(bad)


def test_a_patch_is_applied_only_after_approval_and_a_backup_is_kept(repo):
    original = (repo / "portfolio.py").read_text()
    patch = PendingPatch("portfolio.py", "# fixed\n", "why")
    assert "- " in patch_applier.diff(repo, patch) or "-" in patch_applier.diff(repo, patch)

    assert patch_applier.apply_if_approved(repo, patch, deny_all) is False             # declined
    assert (repo / "portfolio.py").read_text() == original
    assert not (repo / "portfolio.py.bak").exists()

    assert patch_applier.apply_if_approved(repo, patch, lambda a, d: True) is True      # approved
    assert (repo / "portfolio.py").read_text() == "# fixed\n"
    assert (repo / "portfolio.py.bak").read_text() == original                          # easy to roll back
