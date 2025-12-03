package com.marketplace.catalog.service;

import com.marketplace.catalog.model.User;

import java.util.Optional;

/**
 * Контракт сервиса авторизации пользователей.
 */
public interface AuthService {

    /**
     * Выполняет вход пользователя по логину и паролю.
     *
     * @return true, если вход успешен
     */
    Optional<User> login(String login, String password);

    void logout(String login);
}