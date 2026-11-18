package com.ga.hsbc.module2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls the Claude Messages API with the JDK's built-in HttpClient (no SDK, no framework).
 *
 * Config (environment variables):
 *   ANTHROPIC_API_KEY   required - injected into the lab VM, never typed or pasted
 *   CLAUDE_MODEL        optional - defaults to DEFAULT_MODEL; confirm the approved model before delivery
 *   ANTHROPIC_BASE_URL  optional - defaults to https://api.anthropic.com
 */
public final class ClaudeClient implements LlmClient {
    public static final String DEFAULT_MODEL = "claude-sonnet-5-5";
    public static final String DEFAULT_BASE_URL = "https://api.anthropic.com";

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int maxTokens;

    public ClaudeClient(String baseUrl, String apiKey, String model, int maxTokens) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    public static ClaudeClient fromEnv() {
        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("ANTHROPIC_API_KEY is not set");
        String model = System.getenv().getOrDefault("CLAUDE_MODEL", DEFAULT_MODEL);
        String base = System.getenv().getOrDefault("ANTHROPIC_BASE_URL", DEFAULT_BASE_URL);
        return new ClaudeClient(base, key, model, 800);
    }

    @Override
    public LlmResponse complete(String system, String userPrompt) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("system", system);
        body.put("messages", List.of(Map.of("role", "user", "content", userPrompt)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/messages"))
                .timeout(Duration.ofSeconds(60))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(MiniJson.toJson(body)))
                .build();

        long start = System.nanoTime();
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;

        if (response.statusCode() / 100 != 2) {
            String b = response.body();
            throw new IOException("Claude API returned HTTP " + response.statusCode() + ": "
                    + (b.length() > 300 ? b.substring(0, 300) + "..." : b));
        }

        Map<String, Object> json = MiniJson.obj(MiniJson.parse(response.body()));
        StringBuilder text = new StringBuilder();
        for (Object block : MiniJson.arr(json.get("content"))) {
            Map<String, Object> b = MiniJson.obj(block);
            if ("text".equals(b.get("type"))) text.append(MiniJson.str(b.get("text")));
        }
        Map<String, Object> usage = MiniJson.obj(json.get("usage"));
        return new LlmResponse(text.toString(), toInt(usage.get("input_tokens")), toInt(usage.get("output_tokens")), ms);
    }

    private static int toInt(Object o) { return o instanceof Number n ? n.intValue() : 0; }
}
