package com.marketplace.catalog.config;

public interface Config {
    String getDbUrl();
    String getDbUser();
    String getDbPassword();
    String getDbSchema();
    String getLiquibaseChangelog();
}