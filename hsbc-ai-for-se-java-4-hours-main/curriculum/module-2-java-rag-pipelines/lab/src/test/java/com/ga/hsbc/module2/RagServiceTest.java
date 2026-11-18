package com.ga.hsbc.module2;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class RagServiceTest {
    private static InMemoryIndex cached;

    /** Built on first use, so every test fails on its own while the index is still a stub. */
    private static InMemoryIndex index() throws Exception {
        if (cached == null) cached = RagApp.buildIndex(Path.of("sample_docs"));
        return cached;
    }

    static final List<String> ON_TOPIC = List.of(
            "What time is the cutoff for same-day settlement?",
            "What happens to payments in flight when a sanction is applied?",
            "How long do I have to report a failed settlement?",
            "Do client-specific confirmation templates need legal sign-off?",
            "How do I convert kilo bars to troy ounces?",
            "Which column holds the settlement status of a trade?");
    static final List<String> OFF_TOPIC = List.of(
            "What is the best recipe for banana bread?",
            "What is the weather like in London today?",
            "Who won the football match yesterday?",
            "How do I reset my email password?",
            "Recommend a good science fiction movie");

    @Test void theThresholdSeparatesOnTopicFromOffTopic() throws Exception {
        for (String q : ON_TOPIC) assertTrue(index().search(q, 1).get(0).score() >= RagApp.MIN_RELEVANCE, q);
        for (String q : OFF_TOPIC) assertTrue(index().search(q, 1).get(0).score() < RagApp.MIN_RELEVANCE, q);
    }

    @Test void anOnTopicQuestionCallsTheModelOnceWithNumberedSources() throws Exception {
        FakeLlm llm = new FakeLlm("13:00 New York time [1]", 120, 15);
        RagService.RagAnswer answer = RagApp.service(index(), llm).ask("What time is the cutoff for same-day settlement?");
        assertTrue(answer.grounded());
        assertEquals("13:00 New York time [1]", answer.answer());
        assertEquals(120, answer.inputTokens());
        assertEquals(15, answer.outputTokens());
        assertEquals(1, llm.calls.size());
        assertTrue(llm.calls.get(0)[1].contains("<source id=\"1\"") && llm.calls.get(0)[1].contains("cutoff for same-day settlement"));
        assertTrue(llm.calls.get(0)[0].toLowerCase().contains("sources"));
    }

    @Test void anOffTopicQuestionIsDeclinedWithoutCallingTheModel() throws Exception {
        FakeLlm llm = new FakeLlm();
        RagService.RagAnswer answer = RagApp.service(index(), llm).ask("What is the best recipe for banana bread?");
        assertFalse(answer.grounded());
        assertEquals(RagService.NO_ANSWER, answer.answer());
        assertTrue(llm.calls.isEmpty(), "the guardrail saves cost and prevents a guessed answer");
    }
}
