package com.atlas.inventory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Reads vault positions from the inventory_position table. */
public final class InventoryRepository {
    private final Connection connection;

    public InventoryRepository(Connection connection) { this.connection = connection; }

    /** All positions for one metal code, e.g. "AU". */
    public List<Position> findByMetal(String metalCode) throws SQLException {
        String sql = "SELECT vault_id, metal_code, quantity_oz FROM inventory_position WHERE metal_code = '" + metalCode + "'";
        List<Position> result = new ArrayList<>();
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Position(rs.getString("vault_id"), rs.getString("metal_code"), rs.getDouble("quantity_oz")));
            }
        }
        return result;
    }
}
