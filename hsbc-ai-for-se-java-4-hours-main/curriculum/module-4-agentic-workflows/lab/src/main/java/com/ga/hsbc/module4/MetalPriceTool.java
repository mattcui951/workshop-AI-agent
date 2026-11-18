package com.ga.hsbc.module4;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calls the internal price API. Safety controls: metal codes come from a fixed list (so the model cannot
 * steer the URL), the base URL is fixed in configuration, GET only, short timeout.
 */
public final class MetalPriceTool implements Tool {
    private static final Set<String> METALS = Set.of("AU", "AG", "PT", "PD");
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final String baseUrl;

    public MetalPriceTool(String baseUrl) { this.baseUrl = baseUrl; }

    @Override public String name() { return "get_metal_price"; }

    @Override public String description() {
        return "Get the current price per troy ounce for a metal. metal_code must be AU (gold), AG (silver), PT (platinum) or PD (palladium).";
    }

    @Override public Map<String, Object> inputSchema() {
        return Map.of("type", "object",
                "properties", Map.of("metal_code", Map.of("type", "string", "enum", List.of("AU", "AG", "PT", "PD"))),
                "required", List.of("metal_code"));
    }

    @Override public String execute(Map<String, Object> input) throws Exception {
        String code = MiniJson.str(input.get("metal_code"));
        if (code == null || !METALS.contains(code)) throw new IllegalArgumentException("Unsupported metal_code: " + code);
        HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/prices/" + code))
                .timeout(Duration.ofSeconds(5)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IllegalStateException("Price API returned HTTP " + res.statusCode());
        return res.body();
    }
}
