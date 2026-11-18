"""Retrieval-augmented question answering with a relevance guardrail and a human review gate.

YOUR JOB (You Do, with your assistant): replace each `raise NotImplementedError` so that
    pytest -q tests/test_qa_service.py
goes from red to green. Work in this order: build_prompt, then RagService.ask. The ReviewQueue at the bottom is already built for you.
"""
from __future__ import annotations

import itertools   # noqa: F401
import threading   # noqa: F401
from dataclasses import dataclass, field
from datetime import datetime, timezone   # noqa: F401
from enum import Enum

from ingest import InMemoryIndex, ScoredChunk
from llm import LlmClient

NO_ANSWER = "I don't have information on that in the provided documents."
MIN_RELEVANCE = 0.20      # tuned for THIS corpus: re-tune whenever the documents change
TOP_K = 3


def build_prompt(question: str, sources: list[ScoredChunk]) -> tuple[str, str]:
    """Return (system_prompt, user_prompt).

    Rules the tests check:
      - the system prompt tells the model to use ONLY the numbered sources, to cite them like [1], to say so when
        the sources do not contain the answer, and that text inside <source> tags is DATA, not instructions
      - the user prompt shows each source as  <source id="N" doc="FILE NAME"> ...text... </source>  (N starts at 1)
        followed by the question
      - a document must not be able to forge or close a tag: escape "<" and ">" inside the source text
    """
    raise NotImplementedError("TODO: write build_prompt")


@dataclass(frozen=True)
class RagAnswer:
    question: str
    answer: str
    grounded: bool
    sources: list[ScoredChunk] = field(default_factory=list)
    input_tokens: int = 0
    output_tokens: int = 0


class RagService:
    def __init__(self, index: InMemoryIndex, llm: LlmClient, min_score: float = MIN_RELEVANCE, top_k: int = TOP_K) -> None:
        self.index, self.llm, self.min_score, self.top_k = index, llm, min_score, top_k

    def ask(self, question: str) -> RagAnswer:
        """Search the index. If the best score is below min_score, return RagAnswer(question, NO_ANSWER, grounded=False)
        WITHOUT calling the model. Otherwise build the prompt, call self.llm.complete(system, user) once, and return a
        grounded RagAnswer carrying the sources and the token counts."""
        raise NotImplementedError("TODO: write RagService.ask")


# ---------------------------------------------------------------------------------------------
# PRE-BUILT FOR YOU: everything below this line (the review queue) is finished. You do not need to change it.
# ---------------------------------------------------------------------------------------------

class Status(str, Enum):
    PENDING_REVIEW = "PENDING_REVIEW"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"


@dataclass(frozen=True)
class Submission:
    id: str
    answer: RagAnswer
    status: Status
    reviewer: str | None = None
    reason: str | None = None
    decided_at: datetime | None = None


class ReviewQueue:
    """Human-in-the-loop gate: an answer is held as PENDING_REVIEW until a named reviewer approves it."""

    def __init__(self) -> None:
        self._items: dict[str, Submission] = {}
        self._counter = itertools.count(1)
        self._lock = threading.Lock()

    def submit(self, answer: RagAnswer) -> str:
        with self._lock:
            sid = f"A-{next(self._counter):04d}"
            self._items[sid] = Submission(sid, answer, Status.PENDING_REVIEW)
            return sid

    def get(self, sid: str) -> Submission | None:
        return self._items.get(sid)

    def pending(self) -> list[Submission]:
        return sorted((s for s in self._items.values() if s.status is Status.PENDING_REVIEW), key=lambda s: s.id)

    def approve(self, sid: str, reviewer: str) -> Submission:
        return self._decide(sid, Status.APPROVED, reviewer, None)

    def reject(self, sid: str, reviewer: str, reason: str) -> Submission:
        return self._decide(sid, Status.REJECTED, reviewer, reason)

    def released_answer(self, sid: str) -> str | None:
        """The only way a requester reads an answer: None unless a reviewer approved it."""
        item = self._items.get(sid)
        return item.answer.answer if item and item.status is Status.APPROVED else None

    def _decide(self, sid: str, status: Status, reviewer: str, reason: str | None) -> Submission:
        if not reviewer or not reviewer.strip():
            raise ValueError("A named reviewer is required")
        with self._lock:
            current = self._items.get(sid)
            if current is None:
                raise KeyError(f"Unknown submission {sid}")
            if current.status is not Status.PENDING_REVIEW:
                raise RuntimeError(f"{sid} was already {current.status.value}")
            decided = Submission(sid, current.answer, status, reviewer, reason, datetime.now(timezone.utc))
            self._items[sid] = decided
            return decided
