package com.marketplace.catalog.model;

/**
 * Роль пользователя в системе.
 * <ul>
 *     <li>{@link #ADMIN} — полный доступ к каталогу (CRUD-операции);</li>
 *     <li>{@link #VIEWER} — только просмотр и поиск.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    VIEWER
}
