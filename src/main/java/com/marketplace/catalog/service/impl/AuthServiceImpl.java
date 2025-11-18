package com.marketplace.catalog.service.impl;

import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.repository.AuditRepository;
import com.marketplace.catalog.repository.UserRepository;
import com.marketplace.catalog.service.AuthService;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Реализация сервиса авторизации пользователей по умолчанию.
 */
@Getter
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private User currentUser;

    public AuthServiceImpl (UserRepository userRepository, AuditRepository auditRepository) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    public boolean login(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            currentUser = userOpt.get();
            auditRepository.save(new AuditRecord(
                    LocalDateTime.now(),
                    currentUser.getLogin(),
                    "LOGIN",
                    "User logged in"
            ));
            return true;
        }
        return false;
    }

    @Override
    public void logout() {
        if (currentUser != null) {
            auditRepository.save(new AuditRecord(
                    LocalDateTime.now(),
                    currentUser.getLogin(),
                    "LOGOUT",
                    "User logged out"
            ));
        }
        currentUser = null;
    }

    @Override
    public boolean isAdmin() {
        return currentUser != null
                && currentUser.getRole() != null
                && currentUser.getRole().name().equals("ADMIN");
    }
}
