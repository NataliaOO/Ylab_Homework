package com.marketplace.catalog.exception;

/**
 * Исключение, выбрасываемое при некорректных данных товара.
 */
public class ProductValidationException extends RuntimeException {

    public ProductValidationException(String message) {
        super(message);
    }
}