package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.User;

import java.util.Optional;

/**
 * Репозиторий пользователей системы.
 */
public interface UserRepository {

    /**
     * Ищет пользователя по логину.
     *
     * @param login логин пользователя
     * @return Optional с пользователем, если найден
     */
    Optional<User> findByLogin(String login);

    User save(User user);
}
