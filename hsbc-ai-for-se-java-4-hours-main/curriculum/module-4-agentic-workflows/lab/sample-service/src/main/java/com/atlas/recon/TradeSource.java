package com.atlas.recon;

import java.time.LocalDate;
import java.util.List;

/** Where the job reads trades from (a database in production, a fake in tests). */
public interface TradeSource {
    List<TradeRecord> tradesOn(LocalDate businessDate);
}
