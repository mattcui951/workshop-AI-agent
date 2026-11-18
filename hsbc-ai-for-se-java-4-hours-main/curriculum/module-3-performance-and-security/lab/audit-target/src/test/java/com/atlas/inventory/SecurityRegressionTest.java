package com.atlas.inventory;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Security regression tests. They describe how the code SHOULD behave, so on the vulnerable
 * starting code they FAIL. Fix the code until they pass - then keep them forever.
 */
class SecurityRegressionTest {
    private Connection db;

    @BeforeEach void setUp() throws SQLException {
        db = DriverManager.getConnection("jdbc:h2:mem:audit" + System.nanoTime());
        try (Statement st = db.createStatement()) {
            st.execute("CREATE TABLE inventory_position (vault_id VARCHAR(10), metal_code VARCHAR(2), quantity_oz DECIMAL(14,3))");
            st.execute("INSERT INTO inventory_position VALUES ('V-LON','AU',12000), ('V-LON','AG',450000), ('V-NYC','AU',8000), ('V-NYC','PT',1500)");
        }
    }

    // ---- 1. SQL injection ----
    @Test void normalLookupStillWorks() throws SQLException {
        assertEquals(2, new InventoryRepository(db).findByMetal("AU").size());
    }

    @Test void sqlInjectionPayloadReturnsNothing() throws SQLException {
        // A classic payload. A safe repository treats it as a metal code that simply does not exist.
        assertEquals(List.of(), new InventoryRepository(db).findByMetal("AU' OR '1'='1"));
    }

    @Test void sqlInjectionCannotModifyData() throws SQLException {
        try {
            new InventoryRepository(db).findByMetal("AU'; DELETE FROM inventory_position; --");
        } catch (SQLException expectedWhenParameterised) {
            // either outcome is fine as long as nothing was deleted
        }
        assertEquals(2, new InventoryRepository(db).findByMetal("AU").size());
    }

    // ---- 2. Hard-coded credentials ----
    @Test void secretsComeFromTheEnvironmentNotFromSource() {
        assertEquals("from-env", new AppConfig(Map.of("DB_PASSWORD", "from-env", "PRICING_API_KEY", "k")::get).dbPassword());
        assertEquals("k", new AppConfig(Map.of("DB_PASSWORD", "x", "PRICING_API_KEY", "k")::get).pricingApiKey());
    }

    @Test void missingSecretsFailFastInsteadOfFallingBackToADefault() {
        AppConfig empty = new AppConfig(name -> null);
        assertThrows(IllegalStateException.class, empty::dbPassword);
        assertThrows(IllegalStateException.class, empty::pricingApiKey);
    }

    // ---- 3. Sensitive data in output and logs ----
    @Test void statementMasksTheAccountNumber() {
        String out = new StatementExporter().render("12345678901234", List.of(new Position("V-LON", "AU", 12.5)));
        assertFalse(out.contains("12345678901234"), "full account number must not appear in the statement");
        assertTrue(out.contains("1234"), "last four digits are fine to show");
        assertTrue(out.contains("V-LON AU 12.500 oz"), "the positions must still be there");
    }

    @Test void logsNeverContainTheFullAccountNumber() {
        List<String> logged = new ArrayList<>();
        Logger logger = Logger.getLogger(StatementExporter.class.getName());
        Handler capture = new Handler() {
            @Override public void publish(LogRecord r) { logged.add(r.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        };
        logger.addHandler(capture);
        try {
            new StatementExporter().render("12345678901234", List.of());
        } finally {
            logger.removeHandler(capture);
        }
        assertFalse(logged.isEmpty());
        for (String line : logged) assertFalse(line.contains("12345678901234"), "leaked into logs: " + line);
    }
}
