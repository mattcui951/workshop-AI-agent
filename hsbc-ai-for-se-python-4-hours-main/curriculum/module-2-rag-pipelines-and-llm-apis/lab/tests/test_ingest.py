import threading

import pytest

from ingest import (EMBEDDING_DIM, InMemoryIndex, chunk_text, cosine_similarity, embed, load_documents)


def words(n: int) -> str:
    return " ".join(f"word{k}" for k in range(n)) + " "


def run_with_timeout(fn, seconds: float = 2.0):
    """Fail the test (instead of hanging forever) if fn never finishes."""
    box = {}

    def target():
        box["value"] = fn()

    t = threading.Thread(target=target, daemon=True)
    t.start()
    t.join(seconds)
    assert not t.is_alive(), "chunk_text never finished: look at the end of the text"
    return box["value"]


# ---------------- chunking ----------------
def test_chunks_stay_within_size_and_are_never_blank():
    chunks = chunk_text(words(800), 500, 50)
    assert len(chunks) > 3
    assert all(c.strip() for c in chunks)
    assert all(len(c) <= 500 for c in chunks)


def test_no_word_is_lost():
    text = words(800)
    seen = set()
    for c in chunk_text(text, 500, 50):
        seen.update(c.split())
    assert set(text.split()) <= seen


def test_chunks_start_and_end_on_word_boundaries():
    text = words(800)
    for c in chunk_text(text, 500, 50):
        pos = text.find(c)
        assert pos != -1, "a chunk must be an exact slice of the text"
        assert pos == 0 or text[pos - 1].isspace(), f"chunk starts mid-word: {c[:20]!r}"
        end = pos + len(c)
        assert end == len(text) or text[end].isspace(), f"chunk ends mid-word: {c[-20:]!r}"


def test_consecutive_chunks_overlap():
    chunks = chunk_text(words(400), 500, 50)
    assert any(set(a.split()[-3:]) & set(b.split()[:6]) for a, b in zip(chunks, chunks[1:]))


def test_short_text_is_one_chunk():
    assert chunk_text("hello world", 500, 50) == ["hello world"]


def test_always_finishes_even_without_whitespace():
    chunks = run_with_timeout(lambda: chunk_text("x" * 1200, 500, 50))
    assert sum(len(c) for c in chunks) >= 1200


def test_rejects_bad_parameters():
    for size, overlap in [(0, 0), (10, 10), (10, -1)]:
        with pytest.raises(ValueError):
            chunk_text("abc", size, overlap)


# ---------------- embedding ----------------
def test_embedding_has_the_right_size_is_normalised_and_repeatable():
    a, b = embed("same day settlement cutoff"), embed("same day settlement cutoff")
    assert len(a) == EMBEDDING_DIM
    assert a == b
    assert cosine_similarity(a, a) == pytest.approx(1.0)


def test_stopword_only_text_carries_no_signal():
    assert not any(embed("what is the"))


def test_similar_text_scores_higher_than_unrelated_text():
    q = embed("settlement cutoff time")
    assert cosine_similarity(q, embed("the settlement cutoff time is 2:00 PM")) > cosine_similarity(q, embed("banana bread recipe"))


# ---------------- index ----------------
@pytest.fixture(scope="module")
def index():
    idx = InMemoryIndex()
    for name, text in load_documents("sample_docs"):
        idx.add_document(name, text)
    return idx


def test_chunks_are_numbered_from_one_per_document(index):
    first = [h.chunk for h in index.search("settlement", 50) if h.chunk.doc_name == "trade_settlement_policy.txt"]
    assert sorted(c.chunk_no for c in first)[0] == 1
    assert len(index) >= 5


@pytest.mark.parametrize("question, expected_doc", [
    ("What time is the cutoff for same-day settlement?", "trade_settlement_policy.txt"),
    ("How long are reconciliation reports retained?", "payments_reconciliation_guide.txt"),
    ("How many times does the rail retry a notification?", "payment_rail_operations_handbook.txt"),
])
def test_search_finds_the_right_document_first(index, question, expected_doc):
    assert index.search(question, 1)[0].chunk.doc_name == expected_doc


def test_search_returns_best_first_and_respects_top_k(index):
    hits = index.search("reconciliation break escalation", 3)
    assert len(hits) == 3
    assert hits[0].score >= hits[1].score >= hits[2].score
