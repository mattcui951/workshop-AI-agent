package com.ga.hsbc.module4;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SafeSqlTest {
    static Stream<Arguments> accepted() {
        return Stream.of(
                Arguments.of("select * from trade", "select * from trade"),
                Arguments.of("  SELECT trade_id FROM trade;  ", "SELECT trade_id FROM trade"),
                Arguments.of("SELECT metal_code, SUM(quantity_oz) FROM trade GROUP BY metal_code",
                        "SELECT metal_code, SUM(quantity_oz) FROM trade GROUP BY metal_code"),
                Arguments.of("SELECT trade_id, updated_at FROM trade", "SELECT trade_id, updated_at FROM trade"));   // not the word 'update'
    }

    @ParameterizedTest @MethodSource("accepted")
    void acceptsASinglePlainSelect(String sql, String expected) {
        assertEquals(expected, SafeSql.validate(sql));
    }

    static Stream<String> rejected() {
        return Stream.of(
                "DELETE FROM trade", "delete from trade where 1=1", "UPDATE trade SET status='SETTLED'", "INSERT INTO trade VALUES (1)",
                "DROP TABLE trade", "SELECT 1; DROP TABLE trade", "SELECT * FROM trade; SELECT * FROM inventory_position",
                "SELECT * FROM trade -- sneaky", "SELECT /* hi */ * FROM trade", "SELECT * INTO backup FROM trade",
                "SELECT * FROM trade FOR UPDATE", "WITH x AS (SELECT 1) DELETE FROM trade", "CALL SHUTDOWN",
                "SELECT table_name FROM information_schema.tables", "SELECT * FROM INFORMATION_SCHEMA.USERS", "", "   ");
    }

    @ParameterizedTest @MethodSource("rejected")
    void rejectsEverythingElse(String sql) {
        assertThrows(IllegalArgumentException.class, () -> SafeSql.validate(sql), sql);
    }

    @Test void rejectsNullAndVeryLongQueries() {
        assertThrows(IllegalArgumentException.class, () -> SafeSql.validate(null));
        assertThrows(IllegalArgumentException.class, () -> SafeSql.validate("SELECT " + "1,".repeat(2000) + "1"));
    }
}
