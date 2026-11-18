package com.ga.hsbc.module3.recon;

/** A bullion trade. Quantities are in troy ounces. */
public record Trade(String tradeId, String metalCode, double quantityOz, double pricePerOz, String counterpartyId, String status) {
    public double notional() { return quantityOz * pricePerOz; }
}
