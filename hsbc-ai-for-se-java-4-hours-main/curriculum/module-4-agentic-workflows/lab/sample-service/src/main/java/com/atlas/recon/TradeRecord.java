package com.atlas.recon;

import java.time.LocalDate;

/** A settled trade for the overnight reconciliation. Notional is stored in cents to avoid rounding drift. */
public record TradeRecord(String tradeId, LocalDate tradeDate, long notionalCents) {}
