package com.ga.hsbc.module4;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A stand-in for the Claude API that replies with a fixed script, one reply per request, and records what it was sent.
 * It lets us test the agent's loop, approvals and safety controls without a key or a network. It does NOT test
 * the model's judgement - only a live run can do that.
 */
final class ScriptedAnthropic implements AutoCloseable {
    final List<Map<String, Object>> requests = new CopyOnWriteArrayList<>();
    private final List<String> script;
    private final AtomicInteger next = new AtomicInteger();
    private final HttpServer server;

    ScriptedAnthropic(List<String> script) throws IOException { this(script, 0); }

    ScriptedAnthropic(List<String> script, int port) throws IOException {
        this.script = script;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/v1/messages", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.add(MiniJson.obj(MiniJson.parse(body)));
            int k = Math.min(next.getAndIncrement(), script.size() - 1);
            byte[] out = script.get(k).getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("content-type", "application/json");
            ex.sendResponseHeaders(200, out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        server.start();
    }

    String baseUrl() { return "http://127.0.0.1:" + server.getAddress().getPort(); }

    ClaudeToolClient client() { return new ClaudeToolClient(baseUrl(), "test-key", "test-model", 500); }

    @Override public void close() { server.stop(0); }

    // ---- helpers to build replies in the shape the real API uses ----
    static String toolUse(String id, String tool, Map<String, Object> input) {
        return reply("tool_use", List.of(Map.of("type", "tool_use", "id", id, "name", tool, "input", input)));
    }

    static String finalText(String text) {
        return reply("end_turn", List.of(Map.of("type", "text", "text", text)));
    }

    private static String reply(String stopReason, List<Map<String, Object>> content) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", "msg_test");
        m.put("type", "message");
        m.put("role", "assistant");
        m.put("content", new ArrayList<>(content));
        m.put("stop_reason", stopReason);
        m.put("usage", Map.of("input_tokens", 10, "output_tokens", 5));
        return MiniJson.toJson(m);
    }
}
