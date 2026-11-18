package com.ga.hsbc.module2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewQueueTest {
    private static RagService.RagAnswer draft() {
        return new RagService.RagAnswer("q", "draft answer", true, List.of(), 0, 0);
    }

    @Test void anAnswerIsHeldUntilAReviewerApprovesIt() {
        ReviewQueue q = new ReviewQueue();
        String id = q.submit(draft());
        assertEquals("A-0001", id);
        assertEquals(List.of("A-0001"), q.pending().stream().map(ReviewQueue.Submission::id).toList());
        assertTrue(q.releasedAnswer(id).isEmpty());
        ReviewQueue.Submission decided = q.approve(id, "reviewer.one");
        assertEquals(ReviewQueue.Status.APPROVED, decided.status());
        assertEquals("reviewer.one", decided.reviewer());
        assertNotNull(decided.decidedAt());
        assertEquals("draft answer", q.releasedAnswer(id).orElseThrow());
        assertTrue(q.pending().isEmpty());
    }

    @Test void aRejectedAnswerIsNeverReleased() {
        ReviewQueue q = new ReviewQueue();
        String id = q.submit(draft());
        q.reject(id, "reviewer.one", "cites the wrong cutoff");
        assertTrue(q.releasedAnswer(id).isEmpty());
        assertEquals(ReviewQueue.Status.REJECTED, q.get(id).orElseThrow().status());
        assertEquals("cites the wrong cutoff", q.get(id).orElseThrow().reason());
    }

    @Test void aNamedReviewerIsRequired() {
        ReviewQueue q = new ReviewQueue();
        String id = q.submit(draft());
        assertThrows(IllegalArgumentException.class, () -> q.approve(id, ""));
        assertThrows(IllegalArgumentException.class, () -> q.approve(id, "   "));
        assertThrows(IllegalArgumentException.class, () -> q.approve(id, null));
        assertTrue(q.releasedAnswer(id).isEmpty());
    }

    @Test void aDecisionCannotBeChanged() {
        ReviewQueue q = new ReviewQueue();
        String id = q.submit(draft());
        q.reject(id, "reviewer.one", "wrong");
        assertThrows(IllegalStateException.class, () -> q.approve(id, "reviewer.two"));
        assertTrue(q.releasedAnswer(id).isEmpty());
    }

    @Test void unknownIdsAreRejectedAndIdsIncrease() {
        ReviewQueue q = new ReviewQueue();
        assertThrows(IllegalArgumentException.class, () -> q.approve("A-9999", "r"));
        assertEquals("A-0001", q.submit(draft()));
        assertEquals("A-0002", q.submit(draft()));
    }
}
