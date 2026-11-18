package com.ga.hsbc.module3.recon;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Compares your refactored TradeReconciler with the frozen original on lots of random data. */
class OracleEquivalenceTest {
    private final Reconciler legacy = new LegacyTradeReconciler();
    private final Reconciler refactored = new TradeReconciler();

    static List<Trade> randomTrades(int n, int idRange, long seed) {
        Random rnd = new Random(seed);
        String[] metals = {"AU", "AG", "PT", "PD"};
        String[] statuses = {"NEW", "CONFIRMED", "SETTLED", "FAILED", "CANCELLED"};
        List<Trade> out = new ArrayList<>();
        for (int k = 0; k < n; k++) {
            out.add(new Trade("T-" + rnd.nextInt(idRange), metals[rnd.nextInt(4)],
                    Math.round(rnd.nextDouble() * 100000) / 1000.0, Math.round(rnd.nextDouble() * 300000) / 100.0,
                    "CP-" + rnd.nextInt(5), statuses[rnd.nextInt(5)]));
        }
        return out;
    }

    @Test void sameDuplicatesInTheSameOrder() {
        for (long seed = 1; seed <= 20; seed++) {
            List<Trade> trades = randomTrades(400, 300, seed);
            assertEquals(legacy.findDuplicateTradeIds(trades), refactored.findDuplicateTradeIds(trades), "seed " + seed);
        }
    }

    @Test void sameNotionalPerMetal() {
        List<Trade> trades = randomTrades(3000, 100000, 7);
        Map<String, Double> expected = legacy.notionalByMetal(trades);
        Map<String, Double> actual = refactored.notionalByMetal(trades);
        assertEquals(expected.keySet(), actual.keySet());
        // Floating-point sums can differ in the last few bits if the order of addition changes, so compare with a tolerance.
        for (String metal : expected.keySet()) assertEquals(expected.get(metal), actual.get(metal), 1e-3, metal);
    }

    @Test void sameCountsPerStatus() {
        List<Trade> trades = randomTrades(3000, 100000, 9);
        assertEquals(legacy.countByStatus(trades), refactored.countByStatus(trades));
    }
}
