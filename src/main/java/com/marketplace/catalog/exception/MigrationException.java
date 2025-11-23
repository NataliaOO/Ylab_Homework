package com.marketplace.catalog.exception;

/**
 * Ошибка миграций базы данных (Liquibase).
 */
public class MigrationException extends RuntimeException {

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}