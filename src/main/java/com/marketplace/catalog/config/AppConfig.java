package com.marketplace.catalog.config;

import com.marketplace.catalog.exception.ConfigException;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig implements Config {
    private static final String APPLICATION_PROPERTIES = "application.properties";
    private static final String DB_URL      = "db.url";
    private static final String DB_USER     = "db.user";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_SCHEMA   = "db.schema";
    private static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";

    private final Properties props = new Properties();

    public AppConfig() {
        this(APPLICATION_PROPERTIES);
    }

    private AppConfig(String fileName) {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) throw new ConfigException(fileName + " not found");
            props.load(in);
        } catch (Exception e) {
            throw new ConfigException("Failed to load application.properties", e);
        }
    }
    private String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        String v = props.getProperty(key);
        if (v == null) throw new ConfigException("Missing key: " + key);
        return v;
    }

    @Override
    public String getDbUrl() {
        return get(DB_URL);
    }
    @Override
    public String getDbUser() {
        return get(DB_USER);
    }
    @Override
    public String getDbPassword() {
        return get(DB_PASSWORD);
    }
    @Override
    public String getDbSchema() {
        return get(DB_SCHEMA);
    }
    @Override
    public String getLiquibaseChangelog() {
        return get(LIQUIBASE_CHANGELOG);
    }
}
