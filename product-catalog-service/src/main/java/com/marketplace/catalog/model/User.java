package com.marketplace.catalog.model;

/**
 * Пользователь системы каталога.
 * Используется для авторизации и проверки прав доступа.
 */
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

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
