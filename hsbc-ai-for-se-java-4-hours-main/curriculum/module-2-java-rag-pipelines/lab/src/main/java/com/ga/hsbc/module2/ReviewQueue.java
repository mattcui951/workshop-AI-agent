// PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
package com.ga.hsbc.module2;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Human-in-the-loop gate. A grounded answer is held here as PENDING_REVIEW and is only
 * released to the requester after a named reviewer approves it.
 */
public final class ReviewQueue {
    public enum Status { PENDING_REVIEW, APPROVED, REJECTED }

    public record Submission(String id, RagService.RagAnswer answer, Status status,
                             String reviewer, String reason, Instant decidedAt) {}

    private final Map<String, Submission> items = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger();

    public String submit(RagService.RagAnswer answer) {
        String id = String.format("A-%04d", counter.incrementAndGet());
        items.put(id, new Submission(id, answer, Status.PENDING_REVIEW, null, null, null));
        return id;
    }

    public Optional<Submission> get(String id) { return Optional.ofNullable(items.get(id)); }

    public List<Submission> pending() {
        return items.values().stream().filter(s -> s.status() == Status.PENDING_REVIEW)
                .sorted((a, b) -> a.id().compareTo(b.id())).toList();
    }

    public Submission approve(String id, String reviewer) { return decide(id, Status.APPROVED, reviewer, null); }

    public Submission reject(String id, String reviewer, String reason) { return decide(id, Status.REJECTED, reviewer, reason); }

    /** The only way to read an answer's text as a requester: empty unless a reviewer approved it. */
    public Optional<String> releasedAnswer(String id) {
        return get(id).filter(s -> s.status() == Status.APPROVED).map(s -> s.answer().answer());
    }

    private Submission decide(String id, Status status, String reviewer, String reason) {
        if (reviewer == null || reviewer.isBlank()) throw new IllegalArgumentException("A named reviewer is required");
        return items.compute(id, (k, cur) -> {
            if (cur == null) throw new IllegalArgumentException("Unknown submission " + id);
            if (cur.status() != Status.PENDING_REVIEW) throw new IllegalStateException(id + " was already " + cur.status());
            return new Submission(id, cur.answer(), status, reviewer, reason, Instant.now());
        });
    }
}
