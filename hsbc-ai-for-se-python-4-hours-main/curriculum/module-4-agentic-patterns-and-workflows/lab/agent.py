# PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
"""The agent loop (reference solution):

    send the question and the tool list -> if the model asks for a tool: ask a human, run it, send the result back -> repeat.

Safety controls, all visible in this one file:
  1. allow-list   only tools in the registry can run
  2. approval     a human approves every single call
  3. step limit   the loop cannot run forever
  4. errors stay  a failed or declined call is reported to the model as an error result, never hidden
  5. output cap   tool output is truncated before it goes back to the model
  6. audit trail  every call and decision is recorded
"""
from __future__ import annotations

import os
from dataclasses import dataclass

from approval import ApprovalGate
from tools import ToolRegistry

DEFAULT_MODEL = "claude-sonnet-5-5"       # confirm the approved model before delivery (CLAUDE_MODEL overrides it)
MAX_TOOL_OUTPUT_CHARS = 4000


@dataclass(frozen=True)
class ToolCallRecord:
    tool: str
    input: dict
    approved: bool
    error: bool
    result_preview: str


@dataclass(frozen=True)
class AgentResult:
    final_text: str
    stop_reason: str
    steps: int
    audit: list[ToolCallRecord]


def tool_result(tool_use_id: str, content: str, is_error: bool) -> dict:
    block = {"type": "tool_result", "tool_use_id": tool_use_id, "content": content}
    if is_error:
        block["is_error"] = True
    return block


def preview(text: str) -> str:
    return text if len(text) <= 120 else text[:120] + "..."


class ToolAgent:
    def __init__(self, client, registry: ToolRegistry, gate: ApprovalGate, model: str | None = None,
                 max_steps: int = 6, max_tokens: int = 1500) -> None:
        self.client, self.registry, self.gate = client, registry, gate
        self.model = model or os.environ.get("CLAUDE_MODEL", DEFAULT_MODEL)
        self.max_steps, self.max_tokens = max_steps, max_tokens

    def run(self, system: str, question: str) -> AgentResult:
        messages: list[dict] = [{"role": "user", "content": question}]
        audit: list[ToolCallRecord] = []
        for step in range(1, self.max_steps + 1):
            reply = self.client.messages.create(model=self.model, max_tokens=self.max_tokens, system=system,
                                                tools=self.registry.definitions(), messages=messages)
            messages.append({"role": "assistant", "content": reply.content})
            if reply.stop_reason != "tool_use":
                text = "".join(block.text for block in reply.content if block.type == "text")
                return AgentResult(text, reply.stop_reason, step, audit)
            results = [self._handle_tool_call(block, audit) for block in reply.content if block.type == "tool_use"]
            messages.append({"role": "user", "content": results})
        return AgentResult(f"(stopped: reached the limit of {self.max_steps} steps without a final answer)",
                           "max_steps", self.max_steps, audit)

    def _handle_tool_call(self, block, audit: list[ToolCallRecord]) -> dict:
        tool_input = dict(block.input or {})
        tool = self.registry.find(block.name)
        if tool is None:                                                           # 1. allow-list
            audit.append(ToolCallRecord(block.name, tool_input, False, True, "unknown tool"))
            return tool_result(block.id, f"Error: unknown tool '{block.name}'.", True)
        if not self.gate(block.name, tool_input):                                  # 2. approval
            audit.append(ToolCallRecord(block.name, tool_input, False, True, "declined by human reviewer"))
            return tool_result(block.id, "Error: the human reviewer declined this tool call.", True)
        try:
            output = tool.execute(tool_input)
            if len(output) > MAX_TOOL_OUTPUT_CHARS:                                # 5. output cap
                output = output[:MAX_TOOL_OUTPUT_CHARS] + "... [truncated]"
            audit.append(ToolCallRecord(block.name, tool_input, True, False, preview(output)))    # 6. audit trail
            return tool_result(block.id, output, False)
        except Exception as exc:                                                   # 4. errors stay visible to the model
            message = str(exc) or type(exc).__name__
            audit.append(ToolCallRecord(block.name, tool_input, True, True, preview(message)))
            return tool_result(block.id, f"Error: {message}", True)
