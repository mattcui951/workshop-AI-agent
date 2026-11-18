package com.ga.hsbc.module4;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The human gate. Starts RED: PrReviewApp.makeGate() currently approves everything without asking anyone. */
class PrReviewGateTest {
    @Test void safeToolsRunWithoutBotheringTheHuman() {
        ApprovalGate gate = PrReviewApp.makeGate((a, d) -> false);
        for (String tool : PrReviewApp.SAFE_TOOLS) assertTrue(gate.approve(tool, Map.of()), tool);
    }

    @Test void applyingAPatchNeedsTheHumanAndANoMeansNo() {
        assertFalse(PrReviewApp.makeGate((a, d) -> false).approve("apply_patch", Map.of("path", "BusinessDays.java")));
    }

    @Test void theHumanIsActuallyAskedAndTheirYesIsHonoured() {
        List<String> asked = new ArrayList<>();
        ApprovalGate gate = PrReviewApp.makeGate((a, d) -> { asked.add(a); return true; });
        assertTrue(gate.approve("apply_patch", Map.of("path", "BusinessDays.java")));
        assertEquals(List.of("apply_patch"), asked);
    }

    @Test void anUnknownActionIsNeverWavedThrough() {
        assertFalse(PrReviewApp.makeGate((a, d) -> false).approve("delete_everything", Map.of()));
    }
}
