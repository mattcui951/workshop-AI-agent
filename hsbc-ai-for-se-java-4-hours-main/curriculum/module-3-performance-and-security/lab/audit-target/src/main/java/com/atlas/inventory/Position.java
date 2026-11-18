package com.atlas.inventory;

/** One vault's holding of one metal, in troy ounces. */
public record Position(String vaultId, String metalCode, double quantityOz) {}
