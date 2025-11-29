package com.marketplace.catalog.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Пользователь системы каталога.
 * Используется для авторизации и проверки прав доступа.
 */
@Getter
@Setter
@ToString(exclude = "password")
public class User {

    private Long id;
    private String login;
    private String password;
    private Role role;

    /**
     * Создаёт нового пользователя.
     *
     * @param id       идентификатор
     * @param login    логин
     * @param password пароль
     * @param role     роль пользователя
     */
    public User(Long id, String login, String password, Role role) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
    }
}
