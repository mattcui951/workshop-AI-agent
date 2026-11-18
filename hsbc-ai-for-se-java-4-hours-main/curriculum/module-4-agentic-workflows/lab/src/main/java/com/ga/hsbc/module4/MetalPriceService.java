package com.ga.hsbc.module4;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Stand-in for an internal market-data API, so the lab needs no external service.
 * Serves synthetic prices: GET /prices/AU -> {"metal_code":"AU","price_per_oz":2321.5,"currency":"USD"}
 */
public final class MetalPriceService {
    private static final Map<String, Double> PRICES = Map.of("AU", 2321.50, "AG", 29.92, "PT", 1014.00, "PD", 1103.25);
    private final HttpServer server;

    public MetalPriceService(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/prices/", ex -> {
            String code = ex.getRequestURI().getPath().substring("/prices/".length());
            Double price = PRICES.get(code);
            String body = price == null ? "{\"error\":\"unknown metal\"}"
                    : MiniJson.toJson(new java.util.LinkedHashMap<>(Map.of("metal_code", code, "price_per_oz", price, "currency", "USD")));
            byte[] out = body.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("content-type", "application/json");
            ex.sendResponseHeaders(price == null ? 404 : 200, out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        server.start();
    }

    public int port() { return server.getAddress().getPort(); }
    public String baseUrl() { return "http://127.0.0.1:" + port(); }
    public void stop() { server.stop(0); }
}
