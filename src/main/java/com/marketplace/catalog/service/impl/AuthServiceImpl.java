package com.marketplace.catalog.service.impl;

import com.marketplace.catalog.model.User;
import com.marketplace.catalog.repository.UserRepository;
import com.marketplace.catalog.service.AuthService;
import lombok.Getter;

import java.util.Optional;

/**
 * Реализация сервиса авторизации пользователей по умолчанию.
 */
@Getter
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    public AuthServiceImpl (UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> login(String login, String password) {
        return userRepository.findByLogin(login)
                .filter(user -> user.getPassword().equals(password));
    }

    public void logout(String login) {
    }
}
