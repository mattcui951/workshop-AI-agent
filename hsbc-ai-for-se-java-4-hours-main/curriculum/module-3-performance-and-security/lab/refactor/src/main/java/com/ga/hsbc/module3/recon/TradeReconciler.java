package com.ga.hsbc.module3.recon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Works correctly but is slow and verbose. This is the class learners refactor with their AI assistant.
 * The rule: behaviour must not change - the tests in src/test are the safety net.
 */
public class TradeReconciler implements Reconciler {

    @Override
    public List<String> findDuplicateTradeIds(List<Trade> trades) {
        List<String> duplicates = new ArrayList<>();
        for (int i = 0; i < trades.size(); i++) {
            for (int j = i + 1; j < trades.size(); j++) {
                if (trades.get(i).tradeId().equals(trades.get(j).tradeId())) {
                    if (!duplicates.contains(trades.get(i).tradeId())) {
                        duplicates.add(trades.get(i).tradeId());
                    }
                }
            }
        }
        return duplicates;
    }

    @Override
    public Map<String, Double> notionalByMetal(List<Trade> trades) {
        Map<String, Double> totals = new HashMap<>();
        for (Trade t : trades) {
            if (totals.containsKey(t.metalCode())) {
                totals.put(t.metalCode(), totals.get(t.metalCode()) + t.notional());
            } else {
                totals.put(t.metalCode(), t.notional());
            }
        }
        return totals;
    }

    @Override
    public Map<String, Integer> countByStatus(List<Trade> trades) {
        Map<String, Integer> counts = new HashMap<>();
        for (Trade t : trades) {
            if (counts.containsKey(t.status())) {
                counts.put(t.status(), counts.get(t.status()) + 1);
            } else {
                counts.put(t.status(), 1);
            }
        }
        return counts;
    }
}
