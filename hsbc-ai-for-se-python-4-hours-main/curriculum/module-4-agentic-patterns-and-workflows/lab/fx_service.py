"""A stand-in for an internal market-data API, so the lab needs no external service.
Serves SYNTHETIC rates:  GET /rates/USD-EUR  ->  {"pair": "USD/EUR", "rate": 0.92}"""
import json
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer

RATES = {"USD-EUR": 0.92, "EUR-USD": 1.09, "USD-GBP": 0.79, "GBP-USD": 1.27}


class FxService:
    def __init__(self, port: int = 0) -> None:
        class Handler(BaseHTTPRequestHandler):
            def do_GET(self):
                pair = self.path.removeprefix("/rates/")
                rate = RATES.get(pair) if self.path.startswith("/rates/") else None
                body = json.dumps({"error": "unknown pair"} if rate is None else {"pair": pair.replace("-", "/"), "rate": rate}).encode()
                self.send_response(404 if rate is None else 200)
                self.send_header("content-type", "application/json")
                self.send_header("content-length", str(len(body)))
                self.end_headers()
                self.wfile.write(body)

            def log_message(self, *args):
                pass

        self._server = HTTPServer(("127.0.0.1", port), Handler)
        threading.Thread(target=self._server.serve_forever, daemon=True).start()

    @property
    def base_url(self) -> str:
        return f"http://127.0.0.1:{self._server.server_port}"

    def stop(self) -> None:
        self._server.shutdown()
        self._server.server_close()
