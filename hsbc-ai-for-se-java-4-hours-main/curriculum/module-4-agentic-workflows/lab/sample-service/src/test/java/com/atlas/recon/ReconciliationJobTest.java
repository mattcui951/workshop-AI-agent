package com.atlas.recon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReconciliationJobTest {
    /** Trades exist for Friday 2 Oct and Tuesday 6 Oct only - like a real week with a quiet weekend. */
    private static final TradeSource SOURCE = date -> {
        if (date.equals(LocalDate.of(2026, 10, 2))) {
            return List.of(new TradeRecord("T-1", date, 1_000_000), new TradeRecord("T-2", date, 3_000_000));
        }
        return List.of();
    };

    @Test void mondayRunReconcilesFriday() {
        ReconciliationJob.Result r = new ReconciliationJob(SOURCE).run(LocalDate.of(2026, 10, 5));
        assertEquals(LocalDate.of(2026, 10, 2), r.businessDay());
        assertEquals(2, r.tradeCount());
        assertEquals(2_000_000, r.averageNotionalCents());
    }
}
