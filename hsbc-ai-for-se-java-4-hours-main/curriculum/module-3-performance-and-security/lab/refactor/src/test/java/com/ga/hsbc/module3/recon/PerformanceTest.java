package com.ga.hsbc.module3.recon;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Starts RED on purpose: until you fix the algorithm, your TradeReconciler is as slow as the legacy one.
 * (A rough stopwatch check, not a rigorous benchmark - use JMH for real measurements.)
 */
class PerformanceTest {
    private static long bestOf(int runs, Runnable r) {
        long best = Long.MAX_VALUE;
        for (int k = 0; k < runs; k++) {
            long start = System.nanoTime();
            r.run();
            best = Math.min(best, System.nanoTime() - start);
        }
        return best;
    }

    @Test void duplicateDetectionIsAtLeastThreeTimesFasterThanLegacy() {
        List<Trade> trades = OracleEquivalenceTest.randomTrades(8000, 7500, 42);
        Reconciler legacy = new LegacyTradeReconciler();
        Reconciler yours = new TradeReconciler();
        legacy.findDuplicateTradeIds(trades);       // warm-up so the JIT compiler is not part of the measurement
        yours.findDuplicateTradeIds(trades);

        long legacyNs = bestOf(3, () -> legacy.findDuplicateTradeIds(trades));
        long yoursNs = bestOf(3, () -> yours.findDuplicateTradeIds(trades));

        String timings = String.format("legacy=%.1f ms, yours=%.1f ms", legacyNs / 1e6, yoursNs / 1e6);
        assertTrue(yoursNs * 3 < legacyNs, "Not fast enough yet: " + timings);
    }
}
