"""The one thing the QA service needs from a language model.

ClaudeClient calls the Claude Messages API with the official `anthropic` package.
Environment (injected into the lab VM, never typed or pasted):
    ANTHROPIC_API_KEY    required for ClaudeClient
    CLAUDE_MODEL         optional; defaults to DEFAULT_MODEL. Confirm the approved model before delivery.
    ANTHROPIC_BASE_URL   optional; defaults to the public API
"""
from __future__ import annotations

import os
import time
from dataclasses import dataclass
from typing import Protocol

DEFAULT_MODEL = "claude-sonnet-5-5"


@dataclass(frozen=True)
class LlmResponse:
    text: str
    input_tokens: int = 0
    output_tokens: int = 0
    latency_ms: int = 0


class LlmClient(Protocol):
    def complete(self, system: str, user: str) -> LlmResponse: ...


class ClaudeClient:
    def __init__(self, api_key: str, model: str = DEFAULT_MODEL, base_url: str | None = None, max_tokens: int = 800) -> None:
        import anthropic

        self._client = anthropic.Anthropic(api_key=api_key, base_url=base_url)
        self._model, self._max_tokens = model, max_tokens

    @classmethod
    def from_env(cls) -> "ClaudeClient":
        key = os.environ.get("ANTHROPIC_API_KEY")
        if not key:
            raise RuntimeError("ANTHROPIC_API_KEY is not set")
        return cls(key, os.environ.get("CLAUDE_MODEL", DEFAULT_MODEL), os.environ.get("ANTHROPIC_BASE_URL"))

    def complete(self, system: str, user: str) -> LlmResponse:
        start = time.monotonic()
        reply = self._client.messages.create(
            model=self._model, max_tokens=self._max_tokens, system=system,
            messages=[{"role": "user", "content": user}],
        )
        text = "".join(block.text for block in reply.content if block.type == "text")
        return LlmResponse(text, reply.usage.input_tokens, reply.usage.output_tokens, int((time.monotonic() - start) * 1000))


class FakeLlm:
    """A stand-in model for tests: returns a fixed reply and remembers what it was asked."""

    def __init__(self, reply: str = "a fixed reply", input_tokens: int = 10, output_tokens: int = 5) -> None:
        self.reply, self.input_tokens, self.output_tokens = reply, input_tokens, output_tokens
        self.calls: list[tuple[str, str]] = []

    def complete(self, system: str, user: str) -> LlmResponse:
        self.calls.append((system, user))
        return LlmResponse(self.reply, self.input_tokens, self.output_tokens)
