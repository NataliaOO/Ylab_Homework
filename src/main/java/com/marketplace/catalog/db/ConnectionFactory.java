package com.marketplace.catalog.db;

import com.marketplace.catalog.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private static final String URL = AppConfig.get(AppConfig.DB_URL);
    private static final String USER = AppConfig.get(AppConfig.DB_USER);
    private static final String PASSWORD = AppConfig.get(AppConfig.DB_PASSWORD);

    private ConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
