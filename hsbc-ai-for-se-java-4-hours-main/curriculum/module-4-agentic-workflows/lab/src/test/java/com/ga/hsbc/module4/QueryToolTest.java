package com.ga.hsbc.module4;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Needs YOUR SafeSql.validate(), so these stay red until it is written. */
class QueryToolTest {
    @Test void theQueryToolReturnsRowsAsJson() throws Exception {
        QueryTradesTool tool = new QueryTradesTool(Database.agentConnection());
        String json = tool.execute(Map.of("sql", "SELECT SUM(quantity_oz) AS total_oz FROM trade WHERE metal_code = 'AU' AND status = 'SETTLED'"));
        List<Object> rows = MiniJson.arr(MiniJson.parse(json));
        assertEquals(1, rows.size());
        assertEquals(750.0, ((Number) MiniJson.obj(rows.get(0)).get("total_oz")).doubleValue(), 1e-9);
    }

    @Test void theQueryToolRejectsWritesBeforeTouchingTheDatabase() throws Exception {
        QueryTradesTool tool = new QueryTradesTool(Database.agentConnection());
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("sql", "DELETE FROM trade")));
    }

    @Test void theQueryToolCapsTheNumberOfRows() throws Exception {
        QueryTradesTool tool = new QueryTradesTool(Database.agentConnection());
        String json = tool.execute(Map.of("sql", "SELECT a.trade_id FROM trade a, trade b, trade c"));     // 8 x 8 x 8 = 512 rows
        assertEquals(QueryTradesTool.MAX_ROWS, MiniJson.arr(MiniJson.parse(json)).size());
    }

    @Test void theQueryToolCannotBeUsedToMapTheDatabase() throws Exception {
        QueryTradesTool tool = new QueryTradesTool(Database.agentConnection());
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("sql", "SELECT table_name FROM information_schema.tables")));
    }
}
