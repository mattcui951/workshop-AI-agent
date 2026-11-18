# PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
"""Ingestion: load documents, split them into chunks, embed them, and search (reference solution)."""
from __future__ import annotations

import hashlib
import math
import re
from dataclasses import dataclass
from pathlib import Path

EMBEDDING_DIM = 1024

STOPWORDS = {
    "a", "an", "and", "are", "as", "at", "be", "by", "can", "do", "does", "for", "from", "has", "have", "how",
    "i", "if", "in", "is", "it", "my", "me", "of", "on", "or", "our", "the", "to", "was", "we", "what", "when",
    "which", "who", "will", "with", "you", "your", "that", "this", "there", "any", "all", "must", "should",
    "than", "then", "so",
}


@dataclass(frozen=True)
class Chunk:
    doc_name: str
    chunk_no: int
    text: str


@dataclass(frozen=True)
class ScoredChunk:
    chunk: Chunk
    score: float


def load_documents(folder) -> list[tuple[str, str]]:
    """Return (file name, text) for every .txt file in the folder, sorted by name."""
    return [(p.name, p.read_text(encoding="utf-8")) for p in sorted(Path(folder).glob("*.txt"))]


def _last_whitespace(text: str, start_from: int, floor: int) -> int:
    for k in range(start_from, floor, -1):
        if text[k - 1].isspace():
            return k
    return -1


def chunk_text(text: str, size: int = 500, overlap: int = 50) -> list[str]:
    """Split text into overlapping chunks of at most `size` characters, breaking on whitespace."""
    if size <= 0 or overlap < 0 or overlap >= size:
        raise ValueError("need size > 0 and 0 <= overlap < size")
    chunks: list[str] = []
    n = len(text)
    start = 0
    while start < n:
        end = min(start + size, n)
        if end < n:
            ws = _last_whitespace(text, end, start + size // 2)     # prefer to end on whitespace,
            if ws > start:                                            # but never shrink below half a chunk
                end = ws
        piece = text[start:end].strip()
        if piece:
            chunks.append(piece)
        if end >= n:
            break                                                     # reached the end: stop (avoids stalling on the tail)
        nxt = end - overlap
        if nxt <= start:
            nxt = end                                                 # always make progress
        while 0 < nxt < end and not text[nxt - 1].isspace():
            nxt += 1                                                  # start the next chunk on a word boundary
        start = nxt
    return chunks


def embed(text: str) -> list[float]:
    """A deliberately simple 'embedding': hash each meaningful word into a bucket, then normalise.

    This is NOT a real embedding model. It lets the lab run offline and identically everywhere.
    md5 (not Python's hash()) keeps it deterministic across runs.
    """
    vector = [0.0] * EMBEDDING_DIM
    for token in re.split(r"[^a-z0-9_]+", text.lower()):
        if len(token) < 2 or token in STOPWORDS:
            continue
        vector[int(hashlib.md5(token.encode()).hexdigest(), 16) % EMBEDDING_DIM] += 1.0
    norm = math.sqrt(sum(v * v for v in vector))
    return [v / norm for v in vector] if norm else vector


def cosine_similarity(a: list[float], b: list[float]) -> float:
    """Both vectors are already normalised, so this is a dot product."""
    return sum(x * y for x, y in zip(a, b))


class InMemoryIndex:
    """The whole 'vector store': a list of chunks searched by brute force."""

    def __init__(self) -> None:
        self._entries: list[tuple[Chunk, list[float]]] = []

    def __len__(self) -> int:
        return len(self._entries)

    def add_document(self, name: str, text: str, size: int = 500, overlap: int = 50) -> None:
        for number, piece in enumerate(chunk_text(text, size, overlap), start=1):
            chunk = Chunk(name, number, piece)
            self._entries.append((chunk, embed(piece)))

    def search(self, query: str, top_k: int = 3) -> list[ScoredChunk]:
        q = embed(query)
        scored = [ScoredChunk(chunk, cosine_similarity(q, vec)) for chunk, vec in self._entries]
        scored.sort(key=lambda s: s.score, reverse=True)
        return scored[:top_k]
