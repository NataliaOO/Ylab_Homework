package com.marketplace.catalog.config;

/**
 * Абстракция над конфигурацией приложения.
 */
public interface Config {
    /** JDBC URL подключения к базе данных. */
    String getDbUrl();
    /** Имя пользователя для подключения к базе данных. */
    String getDbUser();
    /** Пароль пользователя для подключения к базе данных. */
    String getDbPassword();
    /** Имя схемы, в которой хранятся доменные таблицы. */
    String getDbSchema();
    /** Путь к Liquibase changelog-файлу. */
    String getLiquibaseChangelog();
}