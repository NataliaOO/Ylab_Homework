package com.marketplace.catalog.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Запись аудита, описывающая действие пользователя.
 * Примеры действий: вход, выход, создание/изменение/удаление товара.
 */
public class AuditRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime timestamp;
    private String username;
    private String action;
    private String details;

    /**
     * Создаёт новую запись аудита.
     *
     * @param timestamp время события
     * @param username  имя пользователя
     * @param action    действие
     * @param details   дополнительные детали
     */
    public AuditRecord(LocalDateTime timestamp, String username, String action, String details) {
        this.timestamp = timestamp;
        this.username = username;
        this.action = action;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return timestamp + " | " + username + " | " + action + " | " + details;
    }
}
