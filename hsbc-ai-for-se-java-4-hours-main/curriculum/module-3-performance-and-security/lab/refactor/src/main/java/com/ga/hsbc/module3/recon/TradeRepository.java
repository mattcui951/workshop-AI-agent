package com.ga.hsbc.module3.recon;

import java.time.LocalDate;
import java.util.List;

/** Where trades come from (a database in production). Mocked in the tests. */
public interface TradeRepository {
    List<Trade> findByDate(LocalDate date);
}
