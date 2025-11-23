package com.marketplace.catalog;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.console.ConsoleApp;
import com.marketplace.catalog.db.ConnectionFactory;
import com.marketplace.catalog.db.LiquibaseRunner;
import com.marketplace.catalog.repository.*;
import com.marketplace.catalog.repository.impl.jdbc.*;
import com.marketplace.catalog.service.*;
import com.marketplace.catalog.service.impl.*;

/**
 * Точка входа в приложение Product Catalog Service.
 */
public class Main {

    /**
     * Запускает консольное приложение каталога товаров.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        // 1. Прогоняем миграции Liquibase (создание схем/таблиц/данных)
        LiquibaseRunner.migrate();

        // 2. Репозитории
        AppConfig config = new AppConfig();
        ConnectionFactory connectionFactory = new ConnectionFactory(config);
        ProductRepository productRepository = new JdbcProductRepository(connectionFactory);
        AuditRepository   auditRepository   = new JdbcAuditRepository(connectionFactory);
        UserRepository    userRepository    = new JdbcUserRepository(connectionFactory);

        // 3. Сервисы
        Metrics metrics = new InMemoryMetrics();
        AuthService authService       = new AuthServiceImpl(userRepository, auditRepository);
        ProductService productService = new ProductServiceImpl(productRepository, auditRepository, metrics);
        AuditService auditService     = new AuditServiceImpl(auditRepository);

        // 4. Консольное приложение
        ConsoleApp app = new ConsoleApp(
                authService, productService, auditService, metrics
        );
        app.run();
    }
}
