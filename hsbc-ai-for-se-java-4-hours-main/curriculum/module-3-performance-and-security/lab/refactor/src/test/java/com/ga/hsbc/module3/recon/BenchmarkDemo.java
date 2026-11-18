package com.ga.hsbc.module3.recon;

import java.util.List;

/**
 * Run after your refactor to see WHERE the speed-up came from:
 *   java -cp <classes> com.ga.hsbc.module3.recon.BenchmarkDemo
 * Rough stopwatch timings (best of 5) - good enough to see an order of magnitude, not a rigorous benchmark.
 */
public class BenchmarkDemo {
    static double ms(int runs, Runnable r) {
        long best = Long.MAX_VALUE;
        for (int k = 0; k < runs; k++) {
            long s = System.nanoTime();
            r.run();
            best = Math.min(best, System.nanoTime() - s);
        }
        return best / 1e6;
    }

    public static void main(String[] args) {
        Reconciler legacy = new LegacyTradeReconciler();
        Reconciler yours = new TradeReconciler();

        List<Trade> small = OracleEquivalenceTest.randomTrades(8000, 7500, 42);
        yours.findDuplicateTradeIds(small); legacy.findDuplicateTradeIds(small);
        System.out.printf("findDuplicateTradeIds (8,000 trades):   legacy %8.1f ms | yours %8.1f ms%n",
                ms(5, () -> legacy.findDuplicateTradeIds(small)), ms(5, () -> yours.findDuplicateTradeIds(small)));

        List<Trade> big = OracleEquivalenceTest.randomTrades(400_000, 10_000_000, 43);
        yours.notionalByMetal(big); legacy.notionalByMetal(big);
        System.out.printf("notionalByMetal       (400,000 trades): legacy %8.1f ms | yours %8.1f ms%n",
                ms(5, () -> legacy.notionalByMetal(big)), ms(5, () -> yours.notionalByMetal(big)));
        System.out.printf("countByStatus         (400,000 trades): legacy %8.1f ms | yours %8.1f ms%n",
                ms(5, () -> legacy.countByStatus(big)), ms(5, () -> yours.countByStatus(big)));
    }
}
