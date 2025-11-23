package com.marketplace.catalog.db;

import com.marketplace.catalog.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    public ConnectionFactory(AppConfig config) {
        this.url      = config.get(AppConfig.DB_URL);
        this.user     = config.get(AppConfig.DB_USER);
        this.password = config.get(AppConfig.DB_PASSWORD);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
