package com.ga.hsbc.module4;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Given tests: the finished tools and their built-in controls. They pass from the start. */
class DatabaseAndToolsTest {
    @Test void theDatabaseItselfRefusesWritesFromTheAgentUser() throws Exception {
        // No SafeSql in the way: the read-only account must still be unable to change data.
        try (Connection c = Database.agentConnection(); Statement st = c.createStatement()) {
            SQLException e = assertThrows(SQLException.class, () -> st.execute("DELETE FROM trade"));
            assertTrue(e.getMessage().toLowerCase().contains("right"), e.getMessage());
        }
        try (Connection admin = Database.adminConnection(); Statement st = admin.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM trade")) {
            rs.next();
            assertEquals(8, rs.getInt(1));          // nothing was deleted
        }
    }

    @Test void theDatabaseRefusesToReadFilesEvenWithoutAnyCheck() throws Exception {
        // File functions need admin rights: the agent's account cannot use them. (The metadata schema, however, IS readable by
        // this account, which is why SafeSql also blocks information_schema.)
        try (Connection c = Database.agentConnection(); Statement st = c.createStatement()) {
            assertThrows(SQLException.class, () -> st.executeQuery("SELECT FILE_READ('/etc/hostname', 'UTF-8')"));
            assertThrows(SQLException.class, () -> st.executeQuery("SELECT * FROM CSVREAD('/etc/passwd')"));
        }
    }

    @Test void thePriceToolCallsTheInternalApi() throws Exception {
        MetalPriceService svc = new MetalPriceService(0);
        try {
            Map<String, Object> m = MiniJson.obj(MiniJson.parse(new MetalPriceTool(svc.baseUrl()).execute(Map.of("metal_code", "AU"))));
            assertEquals("AU", m.get("metal_code"));
            assertEquals(2321.5, ((Number) m.get("price_per_oz")).doubleValue(), 1e-9);
        } finally { svc.stop(); }
    }

    @Test void thePriceToolOnlyAcceptsKnownMetalCodes() {
        MetalPriceTool tool = new MetalPriceTool("http://127.0.0.1:1");
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("metal_code", "../admin")));
        assertThrows(IllegalArgumentException.class, () -> tool.execute(Map.of("metal_code", "XX")));
    }
}
