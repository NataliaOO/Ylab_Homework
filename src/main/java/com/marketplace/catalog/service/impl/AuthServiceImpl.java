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
    private User currentUser;

    public AuthServiceImpl (UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean login(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            currentUser = userOpt.get();
            return true;
        }
        return false;
    }

    @Override
    public void logout() {
        currentUser = null;
    }

    @Override
    public boolean isAdmin() {
        return currentUser != null
                && currentUser.getRole() != null
                && currentUser.getRole().name().equals("ADMIN");
    }
}
