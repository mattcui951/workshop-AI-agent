package com.ga.hsbc.module3.recon;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Loads a day's trades and runs the reconciliation checks. */
public class ReconciliationService {
    public record Report(LocalDate date, int tradeCount, List<String> duplicateTradeIds,
                         Map<String, Double> notionalByMetal, Map<String, Integer> countByStatus) {}

    private final TradeRepository repository;
    private final Reconciler reconciler;

    public ReconciliationService(TradeRepository repository, Reconciler reconciler) {
        this.repository = repository;
        this.reconciler = reconciler;
    }

    public Report reconcile(LocalDate date) {
        List<Trade> trades;
        try {
            trades = repository.findByDate(date);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not load trades for " + date, e);
        }
        if (trades == null) trades = List.of();
        return new Report(date, trades.size(), reconciler.findDuplicateTradeIds(trades),
                reconciler.notionalByMetal(trades), reconciler.countByStatus(trades));
    }
}
