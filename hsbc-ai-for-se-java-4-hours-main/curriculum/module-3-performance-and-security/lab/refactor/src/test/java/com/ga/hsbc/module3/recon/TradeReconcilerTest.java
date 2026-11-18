package com.ga.hsbc.module3.recon;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Hand-written behaviour checks: the safety net that must stay green while you refactor TradeReconciler. */
class TradeReconcilerTest {
    private final Reconciler reconciler = new TradeReconciler();

    private static Trade t(String id, String metal, double oz, double price, String status) {
        return new Trade(id, metal, oz, price, "CP-01", status);
    }

    @Test void findsDuplicatesInOrderOfFirstOccurrence() {
        // A B B A: a version that reports ids in order of their SECOND occurrence would answer [B, A] - wrong.
        List<Trade> trades = List.of(t("A", "AU", 1, 1, "NEW"), t("B", "AU", 1, 1, "NEW"),
                t("B", "AU", 1, 1, "NEW"), t("A", "AU", 1, 1, "NEW"), t("C", "AU", 1, 1, "NEW"));
        assertEquals(List.of("A", "B"), reconciler.findDuplicateTradeIds(trades));
    }

    @Test void anIdSeenThreeTimesIsReportedOnce() {
        List<Trade> trades = List.of(t("A", "AU", 1, 1, "NEW"), t("A", "AU", 1, 1, "NEW"), t("A", "AU", 1, 1, "NEW"));
        assertEquals(List.of("A"), reconciler.findDuplicateTradeIds(trades));
    }

    @Test void noDuplicatesAndEmptyInput() {
        assertEquals(List.of(), reconciler.findDuplicateTradeIds(List.of(t("A", "AU", 1, 1, "NEW"), t("B", "AG", 1, 1, "NEW"))));
        assertEquals(List.of(), reconciler.findDuplicateTradeIds(List.of()));
        assertEquals(Map.of(), reconciler.notionalByMetal(List.of()));
        assertEquals(Map.of(), reconciler.countByStatus(List.of()));
    }

    @Test void totalsNotionalPerMetal() {
        List<Trade> trades = List.of(t("1", "AU", 10, 2000, "NEW"), t("2", "AU", 5, 2100, "NEW"), t("3", "AG", 1000, 30, "NEW"));
        Map<String, Double> totals = reconciler.notionalByMetal(trades);
        assertEquals(2, totals.size());
        assertEquals(30_500.0, totals.get("AU"), 1e-6);
        assertEquals(30_000.0, totals.get("AG"), 1e-6);
    }

    @Test void countsTradesPerStatus() {
        List<Trade> trades = List.of(t("1", "AU", 1, 1, "SETTLED"), t("2", "AU", 1, 1, "SETTLED"), t("3", "AU", 1, 1, "FAILED"));
        assertEquals(Map.of("SETTLED", 2, "FAILED", 1), reconciler.countByStatus(trades));
    }
}
