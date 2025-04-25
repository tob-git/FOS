package com.foodapp.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final String CONFIG_FILE = "/application.properties";
    private static final HikariDataSource dataSource;

    static {
        try {
            Properties props = loadProperties();
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.poolSize", "10")));
            
            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize the database connection", e);
        }
    }

    private DatabaseManager() {
        // Private constructor to prevent instantiation
    }

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream inputStream = DatabaseManager.class.getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IOException("Could not find " + CONFIG_FILE);
            }
            props.load(inputStream);
        }
        return props;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
} 