"""A scripted stand-in for the Claude Messages API.

It replies with a fixed script, one reply per request, and remembers what it was sent. It lets us test the agent's loop,
approvals and safety controls with no key and no network. It is NOT a model: it proves the mechanics, never what a real
model will decide. Only a live run can show that.
"""
from __future__ import annotations

import json
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer


def _reply(stop_reason: str, content: list[dict]) -> str:
    return json.dumps({"id": "msg_scripted", "type": "message", "role": "assistant", "model": "scripted", "content": content,
                       "stop_reason": stop_reason, "stop_sequence": None, "usage": {"input_tokens": 10, "output_tokens": 5}})


def tool_use(tool_use_id: str, name: str, tool_input: dict) -> str:
    return _reply("tool_use", [{"type": "tool_use", "id": tool_use_id, "name": name, "input": tool_input}])


def final_text(text: str) -> str:
    return _reply("end_turn", [{"type": "text", "text": text}])


class ScriptedAnthropic:
    def __init__(self, script: list[str], port: int = 0) -> None:
        self.requests: list[dict] = []
        outer = self

        class Handler(BaseHTTPRequestHandler):
            def do_POST(self):
                outer.requests.append(json.loads(self.rfile.read(int(self.headers["content-length"]))))
                index = min(len(outer.requests) - 1, len(script) - 1)       # the last reply repeats forever
                data = script[index].encode()
                self.send_response(200)
                self.send_header("content-type", "application/json")
                self.send_header("content-length", str(len(data)))
                self.end_headers()
                self.wfile.write(data)

            def log_message(self, *args):
                pass

        self._server = HTTPServer(("127.0.0.1", port), Handler)
        threading.Thread(target=self._server.serve_forever, daemon=True).start()

    @property
    def base_url(self) -> str:
        return f"http://127.0.0.1:{self._server.server_port}"

    def close(self) -> None:
        self._server.shutdown()
        self._server.server_close()
