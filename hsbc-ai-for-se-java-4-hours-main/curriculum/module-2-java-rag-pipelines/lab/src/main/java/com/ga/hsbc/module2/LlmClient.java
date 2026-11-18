package com.ga.hsbc.module2;

import java.io.IOException;

/** The one thing the RAG service needs from a language model. Tests use a fake; production uses ClaudeClient. */
public interface LlmClient {
    record LlmResponse(String text, int inputTokens, int outputTokens, long latencyMs) {}

    LlmResponse complete(String system, String userPrompt) throws IOException;
}
