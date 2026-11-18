"""Given test: ClaudeClient builds the right request and parses the reply, checked against a local stand-in for the API."""
import json
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer

import pytest

from llm import ClaudeClient

pytest.importorskip("anthropic")


def serve(status: int, reply: dict):
    seen = {}

    class Handler(BaseHTTPRequestHandler):
        def do_POST(self):
            seen["path"] = self.path
            seen["headers"] = {k.lower(): v for k, v in self.headers.items()}
            seen["body"] = json.loads(self.rfile.read(int(self.headers["content-length"])))
            data = json.dumps(reply).encode()
            self.send_response(status)
            self.send_header("content-type", "application/json")
            self.send_header("content-length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)

        def log_message(self, *args):
            pass

    server = HTTPServer(("127.0.0.1", 0), Handler)
    threading.Thread(target=server.serve_forever, daemon=True).start()
    return server, seen


def test_the_request_and_the_reply_are_handled_correctly():
    reply = {"id": "msg_1", "type": "message", "role": "assistant", "model": "test-model", "stop_reason": "end_turn",
             "stop_sequence": None, "content": [{"type": "text", "text": "Part one. "}, {"type": "text", "text": "Part two."}],
             "usage": {"input_tokens": 42, "output_tokens": 7}}
    server, seen = serve(200, reply)
    try:
        client = ClaudeClient("test-key", "test-model", f"http://127.0.0.1:{server.server_port}", max_tokens=321)
        result = client.complete("be brief", 'hello "world"\nline2')
    finally:
        server.shutdown()
    assert (result.text, result.input_tokens, result.output_tokens) == ("Part one. Part two.", 42, 7)
    assert seen["path"] == "/v1/messages"
    assert seen["headers"]["x-api-key"] == "test-key"
    assert seen["headers"]["anthropic-version"] == "2023-06-01"
    assert seen["body"]["model"] == "test-model" and seen["body"]["max_tokens"] == 321 and seen["body"]["system"] == "be brief"
    assert seen["body"]["messages"] == [{"role": "user", "content": 'hello "world"\nline2'}]
