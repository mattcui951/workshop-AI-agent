package com.atlas.recon;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** Business-day arithmetic for the reconciliation job. (Exchange holidays are out of scope for this sample.) */
public final class BusinessDays {
    private BusinessDays() {}

    /** The most recent business day strictly before {@code date}. */
    public static LocalDate previousBusinessDay(LocalDate date) {
        LocalDate previous = date.minusDays(1);
        if (previous.getDayOfWeek() == DayOfWeek.SUNDAY) {
            previous = previous.minusDays(1);
        }
        return previous;
    }
}
