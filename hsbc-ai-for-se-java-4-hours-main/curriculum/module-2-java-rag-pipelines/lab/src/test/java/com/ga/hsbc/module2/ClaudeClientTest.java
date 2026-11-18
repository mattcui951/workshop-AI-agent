package com.ga.hsbc.module2;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Exercises the real request-building and response-parsing code against a local stand-in for the API. */
class ClaudeClientTest {
    private HttpServer server;

    @AfterEach void stop() { if (server != null) server.stop(0); }

    private String start(int status, String responseJson, AtomicReference<String> method, AtomicReference<String> path,
                         AtomicReference<Map<String, List<String>>> headers, AtomicReference<String> body) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            method.set(ex.getRequestMethod());
            path.set(ex.getRequestURI().getPath());
            headers.set(ex.getRequestHeaders());
            body.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] out = responseJson.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("content-type", "application/json");
            ex.sendResponseHeaders(status, out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @Test void sendsTheMessagesApiRequestAndParsesTheReply() throws Exception {
        var method = new AtomicReference<String>();
        var path = new AtomicReference<String>();
        var headers = new AtomicReference<Map<String, List<String>>>();
        var body = new AtomicReference<String>();
        String reply = "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"content\":["
                + "{\"type\":\"text\",\"text\":\"Part one. \"},{\"type\":\"text\",\"text\":\"Part two.\"}],"
                + "\"stop_reason\":\"end_turn\",\"usage\":{\"input_tokens\":42,\"output_tokens\":7}}";
        String base = start(200, reply, method, path, headers, body);

        ClaudeClient client = new ClaudeClient(base, "test-key", "test-model", 321);
        LlmClient.LlmResponse r = client.complete("be brief", "hello \"world\"\nline2");

        assertEquals("Part one. Part two.", r.text());
        assertEquals(42, r.inputTokens());
        assertEquals(7, r.outputTokens());

        assertEquals("POST", method.get());
        assertEquals("/v1/messages", path.get());
        assertEquals("test-key", headers.get().get("X-api-key").get(0));
        assertEquals("2023-06-01", headers.get().get("Anthropic-version").get(0));

        Map<String, Object> sent = MiniJson.obj(MiniJson.parse(body.get()));
        assertEquals("test-model", sent.get("model"));
        assertEquals(321L, sent.get("max_tokens"));
        assertEquals("be brief", sent.get("system"));
        Map<String, Object> msg = MiniJson.obj(MiniJson.arr(sent.get("messages")).get(0));
        assertEquals("user", msg.get("role"));
        assertEquals("hello \"world\"\nline2", msg.get("content"));     // escaping survives the round trip
    }

    @Test void httpErrorsBecomeIoExceptionsWithoutLeakingTheKey() throws Exception {
        String base = start(401, "{\"error\":{\"type\":\"authentication_error\"}}",
                new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>());
        ClaudeClient client = new ClaudeClient(base, "super-secret-key", "m", 10);
        IOException e = assertThrows(IOException.class, () -> client.complete("s", "u"));
        assertTrue(e.getMessage().contains("401"));
        assertFalse(e.getMessage().contains("super-secret-key"));
    }
}
