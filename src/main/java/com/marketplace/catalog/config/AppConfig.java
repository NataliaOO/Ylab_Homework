package com.marketplace.catalog.config;

import com.marketplace.catalog.exception.ConfigException;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final String DB_URL      = "db.url";
    public static final String DB_USER     = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String DB_SCHEMA   = "db.schema";
    public static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";

    private final Properties PROPS = new Properties();

    public AppConfig() {
        this(APPLICATION_PROPERTIES);
    }

    private AppConfig(String fileName) {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) throw new ConfigException(fileName + " not found");
            PROPS.load(in);
        } catch (Exception e) {
            throw new ConfigException("Failed to load application.properties", e);
        }
    }
    public String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        String v = PROPS.getProperty(key);
        if (v == null) throw new ConfigException("Missing key: " + key);
        return v;
    }
}
