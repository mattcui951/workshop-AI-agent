package com.ga.hsbc.module2;

import java.util.ArrayList;
import java.util.List;

/** A stand-in model for tests: returns a fixed reply and remembers what it was asked. */
final class FakeLlm implements LlmClient {
    final List<String[]> calls = new ArrayList<>();
    private final String reply;
    private final int inputTokens, outputTokens;

    FakeLlm(String reply, int inputTokens, int outputTokens) {
        this.reply = reply;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    FakeLlm() { this("a fixed reply", 10, 5); }

    @Override public LlmResponse complete(String system, String userPrompt) {
        calls.add(new String[] {system, userPrompt});
        return new LlmResponse(reply, inputTokens, outputTokens, 1);
    }
}
