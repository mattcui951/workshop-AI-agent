package com.ga.hsbc.module3.recon;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Mockito replaces the database so the service can be tested alone. */
class ReconciliationServiceTest {
    private static final LocalDate DAY = LocalDate.of(2026, 10, 2);

    @Test void buildsAReportFromTheRepositoryTrades() {
        TradeRepository repo = mock(TradeRepository.class);
        when(repo.findByDate(DAY)).thenReturn(List.of(
                new Trade("A", "AU", 10, 2000, "CP-01", "SETTLED"),
                new Trade("A", "AU", 10, 2000, "CP-01", "SETTLED"),
                new Trade("B", "AG", 1000, 30, "CP-02", "FAILED")));

        ReconciliationService.Report report = new ReconciliationService(repo, new TradeReconciler()).reconcile(DAY);

        assertEquals(3, report.tradeCount());
        assertEquals(List.of("A"), report.duplicateTradeIds());
        assertEquals(40_000.0, report.notionalByMetal().get("AU"), 1e-6);
        verify(repo, times(1)).findByDate(DAY);
    }

    @Test void aMissingResultBecomesAnEmptyReport() {
        TradeRepository repo = mock(TradeRepository.class);
        when(repo.findByDate(any())).thenReturn(null);
        ReconciliationService.Report r = new ReconciliationService(repo, new TradeReconciler()).reconcile(DAY);
        assertEquals(0, r.tradeCount());
        assertTrue(r.duplicateTradeIds().isEmpty());
    }

    @Test void repositoryFailuresAreWrappedWithContext() {
        TradeRepository repo = mock(TradeRepository.class);
        when(repo.findByDate(any())).thenThrow(new RuntimeException("db down"));
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new ReconciliationService(repo, new TradeReconciler()).reconcile(DAY));
        assertTrue(e.getMessage().contains("2026-10-02"));
        assertEquals("db down", e.getCause().getMessage());
    }
}
