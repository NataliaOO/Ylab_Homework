package com.marketplace.catalog.service.api;

import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.repository.AuditRepository;
import com.marketplace.catalog.repository.UserRepository;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Сервис авторизации пользователей.
 */
@Getter
public class AuthService {

    private final UserRepository userRepository;
    private final AuditRepository auditRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository, AuditRepository auditRepository) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Выполняет вход пользователя по логину и паролю.
     */
    public boolean login(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            currentUser = userOpt.get();
            auditRepository.save(new AuditRecord(LocalDateTime.now(), currentUser.getLogin(),
                    "LOGIN", "User logged in"));
            return true;
        }
        return false;
    }

    /**
     * Выполняет выход текущего пользователя.
     */
    public void logout() {
        if (currentUser != null) {
            auditRepository.save(new AuditRecord(LocalDateTime.now(), currentUser.getLogin(),
                    "LOGOUT", "User logged out"));
        }
        currentUser = null;
    }

    /**
     * Проверяет, является ли текущий пользователь администратором.
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() != null
                && currentUser.getRole().name().equals("ADMIN");
    }
}
