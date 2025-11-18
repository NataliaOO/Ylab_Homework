package com.marketplace.catalog.service;

import com.marketplace.catalog.model.User;

/**
 * Контракт сервиса авторизации пользователей.
 */
public interface AuthService {

    /**
     * Выполняет вход пользователя по логину и паролю.
     *
     * @return true, если вход успешен
     */
    boolean login(String login, String password);

    /**
     * Выполняет выход текущего пользователя.
     */
    void logout();

    /**
     * Является ли текущий пользователь администратором.
     */
    boolean isAdmin();

    /**
     * Текущий авторизованный пользователь (может быть null).
     */
    User getCurrentUser();
}