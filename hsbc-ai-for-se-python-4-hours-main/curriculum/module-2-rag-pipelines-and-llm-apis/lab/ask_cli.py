"""Ask the document set a question from the command line, with a human review step.

    python3 ask_cli.py "What time is the cutoff for same-day settlement?"

Retrieval always runs. The answer step needs ANTHROPIC_API_KEY. A grounded answer is a DRAFT until you approve it.
"""
import os
import sys

from ingest import InMemoryIndex, load_documents
from qa_service import MIN_RELEVANCE, TOP_K, RagService, ReviewQueue


def build_index(folder: str = "sample_docs") -> InMemoryIndex:
    index = InMemoryIndex()
    for name, text in load_documents(folder):
        index.add_document(name, text)
    return index


def main() -> None:
    question = " ".join(sys.argv[1:]) or "What time is the cutoff for same-day settlement?"
    index = build_index()
    print(f"Indexed {len(index)} chunks\nQuestion: {question}\n--- Top retrieved chunks ---")
    for hit in index.search(question, TOP_K):
        preview = hit.chunk.text.replace("\n", " ")[:90]
        print(f"(score={hit.score:.3f}) {hit.chunk.doc_name}#{hit.chunk.chunk_no}  {preview}...")
    if hit_best(index, question) < MIN_RELEVANCE:
        print(f"\n[guardrail] best score is below {MIN_RELEVANCE}: the model will not be called.")

    if not os.environ.get("ANTHROPIC_API_KEY"):
        print("\n(Set ANTHROPIC_API_KEY to run the answer step.)")
        return

    from llm import ClaudeClient

    answer = RagService(index, ClaudeClient.from_env()).ask(question)
    print(f"\n--- Draft answer (grounded={answer.grounded}) ---\n{answer.answer}")
    print(f"[metrics] input_tokens={answer.input_tokens} output_tokens={answer.output_tokens}")
    if not answer.grounded:
        return

    queue = ReviewQueue()
    sid = queue.submit(answer)
    print(f"\nThis is a DRAFT ({sid}). It is not released until a reviewer approves it.")
    reviewer = input("Reviewer name: ").strip()
    if input("Approve this answer? (y/n): ").strip().lower() == "y":
        queue.approve(sid, reviewer)
        print(f"\nReleased to the requester:\n{queue.released_answer(sid)}")
    else:
        queue.reject(sid, reviewer, "rejected by reviewer")
        print("\nRejected. Nothing was released.")


def hit_best(index: InMemoryIndex, question: str) -> float:
    hits = index.search(question, 1)
    return hits[0].score if hits else 0.0


if __name__ == "__main__":
    main()
