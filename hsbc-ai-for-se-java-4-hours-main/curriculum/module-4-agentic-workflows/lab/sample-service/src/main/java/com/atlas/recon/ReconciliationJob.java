package com.atlas.recon;

import java.time.LocalDate;
import java.util.List;

/** Runs every business morning at 02:00 and reports the average notional of the previous business day's trades. */
public final class ReconciliationJob {
    public record Result(LocalDate businessDay, int tradeCount, long averageNotionalCents) {}

    private final TradeSource source;

    public ReconciliationJob(TradeSource source) { this.source = source; }

    public Result run(LocalDate runDate) {
        LocalDate businessDay = BusinessDays.previousBusinessDay(runDate);
        List<TradeRecord> trades = source.tradesOn(businessDay);
        long average = averageNotionalCents(trades);
        return new Result(businessDay, trades.size(), average);
    }

    static long averageNotionalCents(List<TradeRecord> trades) {
        long total = 0;
        for (TradeRecord t : trades) {
            total += t.notionalCents();
        }
        return total / trades.size();
    }
}
