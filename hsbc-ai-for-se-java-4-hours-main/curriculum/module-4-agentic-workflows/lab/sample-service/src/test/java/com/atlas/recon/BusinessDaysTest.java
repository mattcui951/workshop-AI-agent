package com.atlas.recon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BusinessDaysTest {
    @Test void tuesdayGoesBackToMonday() {
        assertEquals(LocalDate.of(2026, 10, 5), BusinessDays.previousBusinessDay(LocalDate.of(2026, 10, 6)));
    }

    @Test void mondayGoesBackToFriday() {
        assertEquals(LocalDate.of(2026, 10, 2), BusinessDays.previousBusinessDay(LocalDate.of(2026, 10, 5)));
    }

    @Test void sundayGoesBackToFriday() {
        assertEquals(LocalDate.of(2026, 10, 2), BusinessDays.previousBusinessDay(LocalDate.of(2026, 10, 4)));
    }

    @Test void saturdayGoesBackToFriday() {
        assertEquals(LocalDate.of(2026, 10, 2), BusinessDays.previousBusinessDay(LocalDate.of(2026, 10, 3)));
    }
}
