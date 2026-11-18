package com.ga.hsbc.module4;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Claude Messages API with tool use, over the JDK HttpClient.
 *
 * Environment: ANTHROPIC_API_KEY (required), CLAUDE_MODEL (default below - confirm the approved model),
 * ANTHROPIC_BASE_URL (default https://api.anthropic.com).
 */
public final class ClaudeToolClient {
    public static final String DEFAULT_MODEL = "claude-sonnet-5-5";
    public static final String DEFAULT_BASE_URL = "https://api.anthropic.com";

    public record ToolResponse(String stopReason, List<Map<String, Object>> content, int inputTokens, int outputTokens) {
        public String text() {
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> b : content) if ("text".equals(b.get("type"))) sb.append(MiniJson.str(b.get("text")));
            return sb.toString();
        }
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl, apiKey, model;
    private final int maxTokens;

    public ClaudeToolClient(String baseUrl, String apiKey, String model, int maxTokens) {
        this.baseUrl = baseUrl; this.apiKey = apiKey; this.model = model; this.maxTokens = maxTokens;
    }

    public static ClaudeToolClient fromEnv() {
        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("ANTHROPIC_API_KEY is not set");
        return new ClaudeToolClient(System.getenv().getOrDefault("ANTHROPIC_BASE_URL", DEFAULT_BASE_URL), key,
                System.getenv().getOrDefault("CLAUDE_MODEL", DEFAULT_MODEL), 1500);
    }

    public ToolResponse send(String system, List<Map<String, Object>> messages, List<Map<String, Object>> tools) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("system", system);
        body.put("tools", tools);
        body.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/messages"))
                .timeout(Duration.ofSeconds(90))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(MiniJson.toJson(body)))
                .build();
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
        if (response.statusCode() / 100 != 2) {
            String b = response.body();
            throw new IOException("Claude API returned HTTP " + response.statusCode() + ": " + (b.length() > 300 ? b.substring(0, 300) + "..." : b));
        }
        Map<String, Object> json = MiniJson.obj(MiniJson.parse(response.body()));
        List<Map<String, Object>> content = new ArrayList<>();
        for (Object o : MiniJson.arr(json.get("content"))) content.add(MiniJson.obj(o));
        Map<String, Object> usage = MiniJson.obj(json.get("usage"));
        return new ToolResponse(MiniJson.str(json.get("stop_reason")), content,
                usage.get("input_tokens") instanceof Number n ? n.intValue() : 0,
                usage.get("output_tokens") instanceof Number n ? n.intValue() : 0);
    }
}
