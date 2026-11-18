package com.ga.hsbc.module4;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only SQL tool. Defence in depth: SafeSql check + read-only database user + row limit + timeout. */
public final class QueryTradesTool implements Tool {
    public static final int MAX_ROWS = 50;
    private final Connection readOnlyConnection;

    public QueryTradesTool(Connection readOnlyConnection) { this.readOnlyConnection = readOnlyConnection; }

    @Override public String name() { return "query_trade_db"; }

    @Override public String description() {
        return "Run ONE read-only SQL SELECT against the trading database. Tables: "
                + "trade(trade_id, metal_code, quantity_oz, price_per_oz, currency, counterparty_id, trade_date, settlement_date, status) "
                + "where metal_code is AU, AG, PT or PD and status is NEW, CONFIRMED, SETTLED, FAILED or CANCELLED; "
                + "inventory_position(vault_id, metal_code, quantity_oz, as_of_date). Returns at most " + MAX_ROWS + " rows.";
    }

    @Override public Map<String, Object> inputSchema() {
        return Map.of("type", "object",
                "properties", Map.of("sql", Map.of("type", "string", "description", "A single SELECT statement")),
                "required", List.of("sql"));
    }

    @Override public String execute(Map<String, Object> input) throws Exception {
        String sql = SafeSql.validate(MiniJson.str(input.get("sql")));
        try (Statement st = readOnlyConnection.createStatement()) {
            st.setMaxRows(MAX_ROWS);
            st.setQueryTimeout(5);
            try (ResultSet rs = st.executeQuery(sql)) {
                ResultSetMetaData md = rs.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int c = 1; c <= md.getColumnCount(); c++) {
                        Object v = rs.getObject(c);
                        row.put(md.getColumnLabel(c).toLowerCase(), v == null || v instanceof Number ? v : String.valueOf(v));
                    }
                    rows.add(row);
                }
                return MiniJson.toJson(rows);
            }
        }
    }
}
