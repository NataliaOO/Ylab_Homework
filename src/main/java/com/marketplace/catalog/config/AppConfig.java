package com.marketplace.catalog.config;

import com.marketplace.catalog.exception.ConfigException;
import org.springframework.core.env.Environment;

public class AppConfig implements Config {
    private static final String DB_URL      = "db.url";
    private static final String DB_USER     = "db.user";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_SCHEMA   = "db.schema";
    private static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";

    private final Environment env;

    public AppConfig(Environment env) {
        this.env = env;
    }

    private String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null) {
            return sys;
        }
        String v = env.getProperty(key);
        if (v == null) {
            throw new ConfigException("Missing key: " + key);
        }
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
