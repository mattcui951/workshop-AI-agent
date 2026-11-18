package com.ga.hsbc.module4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Embedded in-memory H2 database with synthetic data - nothing to install or provision.
 *
 * Two accounts, on purpose:
 *   - admin ("sa") creates and seeds the tables, and is NEVER handed to the agent
 *   - AGENT_RO can only SELECT from the two approved tables
 * Even if a bad query slipped past SafeSql, the database itself refuses to write. That is least privilege.
 * (Demo-only credentials: this database lives in memory and holds synthetic rows. Real services read
 * credentials from a secrets manager or environment, never from source.)
 */
public final class Database {
    /** Admin URL keeps the in-memory database alive between connections (DB_CLOSE_DELAY=-1 is an admin-only setting). */
    public static final String ADMIN_URL = "jdbc:h2:mem:atlas;DB_CLOSE_DELAY=-1";
    /** The agent connects to the SAME database by name, with no admin-only settings in its URL. */
    public static final String AGENT_URL = "jdbc:h2:mem:atlas";
    public static final String AGENT_USER = "AGENT_RO";
    private static final String AGENT_PASSWORD = "demo";
    private static boolean initialised = false;

    private Database() {}

    public static synchronized void init() throws SQLException {
        if (initialised) return;
        try (Connection admin = DriverManager.getConnection(ADMIN_URL, "sa", "");
             Statement st = admin.createStatement()) {
            st.execute("""
                    CREATE TABLE trade (
                      trade_id VARCHAR(20) PRIMARY KEY, metal_code VARCHAR(2), quantity_oz DECIMAL(14,3),
                      price_per_oz DECIMAL(12,2), currency VARCHAR(3), counterparty_id VARCHAR(10),
                      trade_date DATE, settlement_date DATE, status VARCHAR(12))""");
            st.execute("""
                    CREATE TABLE inventory_position (
                      vault_id VARCHAR(10), metal_code VARCHAR(2), quantity_oz DECIMAL(14,3), as_of_date DATE)""");
            st.execute("""
                    INSERT INTO trade VALUES
                    ('T-100001','AU',500.000,2315.40,'USD','CP-01','2026-09-28','2026-09-30','SETTLED'),
                    ('T-100002','AU',250.000,2318.10,'USD','CP-02','2026-09-29','2026-10-01','SETTLED'),
                    ('T-100003','AG',10000.000,29.85,'USD','CP-01','2026-09-29','2026-10-01','SETTLED'),
                    ('T-100004','PT',120.000,1012.50,'USD','CP-03','2026-09-30','2026-10-02','CONFIRMED'),
                    ('T-100005','AU',100.000,2320.00,'USD','CP-03','2026-09-30','2026-10-02','FAILED'),
                    ('T-100006','PD',80.000,1105.75,'USD','CP-02','2026-10-01','2026-10-03','NEW'),
                    ('T-100007','AU',300.000,2322.60,'USD','CP-01','2026-10-01','2026-10-03','CANCELLED'),
                    ('T-100008','AG',5000.000,29.90,'USD','CP-02','2026-10-02','2026-10-06','CONFIRMED')""");
            st.execute("""
                    INSERT INTO inventory_position VALUES
                    ('V-LON','AU',12000.000,'2026-10-02'), ('V-LON','AG',450000.000,'2026-10-02'),
                    ('V-NYC','AU',8000.000,'2026-10-02'), ('V-NYC','PT',1500.000,'2026-10-02')""");
            st.execute("CREATE USER " + AGENT_USER + " PASSWORD '" + AGENT_PASSWORD + "'");
            st.execute("GRANT SELECT ON trade TO " + AGENT_USER);
            st.execute("GRANT SELECT ON inventory_position TO " + AGENT_USER);
        }
        initialised = true;
    }

    /** Admin connection - for setup and for tests that check the data. Never give this to an agent. */
    public static Connection adminConnection() throws SQLException {
        init();
        return DriverManager.getConnection(ADMIN_URL, "sa", "");
    }

    /** The read-only connection the agent's SQL tool uses. */
    public static Connection agentConnection() throws SQLException {
        init();
        return DriverManager.getConnection(AGENT_URL, AGENT_USER, AGENT_PASSWORD);
    }
}
