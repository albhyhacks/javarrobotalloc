package com.warehouse.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// One shared MySQL connection for the whole program, plus a helper for UPDATE/INSERT/DELETE.
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/warehouse_db";
    private static final String USER = "root";
    private static final String PASS = "Warehouse@2026";

    private static Connection conn;

    // Opens the connection the first time, or again if it was closed.
    public static Connection get() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
        return conn;
    }

    // Runs one SQL write. Each "?" in the sql is filled with the next value, in order.
    // Example: run("UPDATE robots SET status = ? WHERE id = ?", "BUSY", 3);
    public static void run(String sql, Object... values) throws SQLException {
        try (PreparedStatement ps = get().prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }
            ps.executeUpdate();
        }
    }
}
