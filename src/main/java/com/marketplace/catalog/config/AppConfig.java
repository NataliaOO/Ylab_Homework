package com.marketplace.catalog.config;

import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
    public static final String APPLICATION_PROPERTIES = "application.properties";
    public static final String DB_URL      = "db.url";
    public static final String DB_USER     = "db.user";
    public static final String DB_PASSWORD = "db.password";
    public static final String DB_SCHEMA   = "db.schema";
    public static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            if (in == null) throw new IllegalStateException(APPLICATION_PROPERTIES + " not found");
            PROPS.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }
    private AppConfig() {}
    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        String v = PROPS.getProperty(key);
        if (v == null) throw new IllegalArgumentException("Missing key: " + key);
        return v;
    }
}
