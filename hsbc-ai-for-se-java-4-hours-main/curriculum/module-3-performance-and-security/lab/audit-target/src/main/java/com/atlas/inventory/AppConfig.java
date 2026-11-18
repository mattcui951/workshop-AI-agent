package com.atlas.inventory;

import java.util.function.Function;

/** Reads service configuration. */
public final class AppConfig {
    private static final String DB_PASSWORD = "atlas-prod-Passw0rd!";
    private static final String PRICING_API_KEY = "FAKE-API-KEY-1234567890";

    public AppConfig() {}

    /** Test seam: lets tests supply their own environment. */
    public AppConfig(Function<String, String> environment) {}

    public String dbPassword() { return DB_PASSWORD; }

    public String pricingApiKey() { return PRICING_API_KEY; }
}
