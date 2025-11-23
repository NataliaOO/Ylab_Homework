package com.marketplace.catalog.exception;

/**
 * Обёртка над SQL-ошибками в репозиториях.
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
