package com.foodapp.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // Using Windows Authentication
    private static final String URL = "jdbc:sqlserver://localhost:1434;databaseName=FOS;integratedSecurity=true;encrypt=false";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQL Server JDBC driver", e);
        }
    }

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void closeDataSource() {
        // Close resources if needed
    }
}
