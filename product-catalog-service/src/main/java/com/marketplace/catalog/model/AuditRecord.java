package com.marketplace.catalog.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Запись аудита, описывающая действие пользователя.
 * Примеры действий: вход, выход, создание/изменение/удаление товара.
 *
 * @param timestamp время события
 * @param username  логин пользователя
 * @param action    тип события (например, LOGIN, LOGOUT, CREATE_PRODUCT)
 * @param details   дополнительные детали события
 */
public record AuditRecord(
        LocalDateTime timestamp,
        String username,
        String action,
        String details
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
