"""The only code that writes to the repository, and only after a human says yes. Keeps a .bak of the old file."""
from __future__ import annotations

import difflib
import shutil
from pathlib import Path

from approval import ApprovalGate
from path_guard import resolve_inside
from repo_tools import PendingPatch


def diff(root: Path, patch: PendingPatch) -> str:
    target = resolve_inside(root, patch.path)
    old = target.read_text(encoding="utf-8").splitlines(keepends=True)
    new = patch.new_content.splitlines(keepends=True)
    return "".join(difflib.unified_diff(old, new, fromfile=f"a/{patch.path}", tofile=f"b/{patch.path}")) or "(no changes)"


def apply_if_approved(root: Path, patch: PendingPatch, gate: ApprovalGate) -> bool:
    """Returns True only if the file was changed."""
    target = resolve_inside(root, patch.path)
    if not gate("apply_patch", {"path": patch.path, "explanation": patch.explanation}):
        return False
    shutil.copy2(target, target.with_name(target.name + ".bak"))
    target.write_text(patch.new_content, encoding="utf-8")
    return True
