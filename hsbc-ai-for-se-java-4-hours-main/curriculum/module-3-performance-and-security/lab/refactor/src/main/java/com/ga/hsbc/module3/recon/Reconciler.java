package com.ga.hsbc.module3.recon;

import java.util.List;
import java.util.Map;

/** The three checks the end-of-day reconciliation needs. */
public interface Reconciler {
    /** Trade ids that appear more than once, each listed once, in the order their FIRST occurrence appears. */
    List<String> findDuplicateTradeIds(List<Trade> trades);

    /** Total notional (quantity x price) per metal code. */
    Map<String, Double> notionalByMetal(List<Trade> trades);

    /** Number of trades per status. */
    Map<String, Integer> countByStatus(List<Trade> trades);
}
