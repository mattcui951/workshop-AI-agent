"""File tools for the PR-review agent. The agent can read and PROPOSE; it has no tool that writes."""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from path_guard import resolve_inside

MAX_FILE_CHARS = 20_000


@dataclass(frozen=True)
class PendingPatch:
    """A fix the agent PROPOSED. Nothing has been written to disk."""
    path: str
    new_content: str
    explanation: str


class _Tool:
    def __init__(self, name: str, description: str, input_schema: dict, body) -> None:
        self.name, self.description, self.input_schema, self._body = name, description, input_schema, body

    def execute(self, tool_input: dict) -> str:
        return self._body(tool_input)


_NO_INPUT = {"type": "object", "properties": {}}


def list_files(root: Path) -> _Tool:
    def body(_):
        base = Path(root).resolve()
        names = sorted(str(p.relative_to(base)) for p in base.rglob("*")
                       if p.is_file() and "__pycache__" not in p.parts and (p.suffix == ".py" or p.name == "ci-report.txt"))
        return "\n".join(names[:200])
    return _Tool("list_files", "List the Python source files and the CI report in the repository.", _NO_INPUT, body)


def read_file(root: Path) -> _Tool:
    def body(tool_input):
        path = resolve_inside(root, tool_input.get("path"))
        if path.suffix != ".py":
            raise ValueError("Only Python files can be read")
        text = path.read_text(encoding="utf-8")
        return text if len(text) <= MAX_FILE_CHARS else text[:MAX_FILE_CHARS] + "\n... [truncated]"
    return _Tool("read_file", "Read one Python file. Input: path relative to the repository root.",
                 {"type": "object", "properties": {"path": {"type": "string"}}, "required": ["path"]}, body)


def read_ci_report(root: Path) -> _Tool:
    def body(_):
        text = resolve_inside(root, "ci-report.txt").read_text(encoding="utf-8")
        return text[-8000:]
    return _Tool("read_ci_report", "Read last night's CI report.", _NO_INPUT, body)


def propose_patch(root: Path, sink: list[PendingPatch]) -> _Tool:
    def body(tool_input):
        path = resolve_inside(root, tool_input.get("path"))
        relative = path.relative_to(Path(root).resolve())
        if path.suffix != ".py" or relative.parts[0] == "tests":
            raise ValueError("Patches are only accepted for Python source files outside tests/")
        if not path.is_file():
            raise ValueError(f"File does not exist: {relative}")
        content = tool_input.get("new_content")
        if not content or not str(content).strip() or len(content) > MAX_FILE_CHARS:
            raise ValueError("new_content is missing or too large")
        sink.append(PendingPatch(str(relative), content, str(tool_input.get("explanation", ""))))
        return "Proposal recorded for human review. It has NOT been applied."
    return _Tool("propose_patch",
                 "Propose a fix by giving the COMPLETE corrected content of one Python file (not under tests/) plus a short "
                 "explanation. This only records a proposal for human review; nothing is changed.",
                 {"type": "object", "properties": {"path": {"type": "string"}, "new_content": {"type": "string"},
                                                    "explanation": {"type": "string"}},
                  "required": ["path", "new_content", "explanation"]}, body)
