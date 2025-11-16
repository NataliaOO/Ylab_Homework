package com.marketplace.catalog;

import com.marketplace.catalog.console.ConsoleApp;
import com.marketplace.catalog.db.LiquibaseRunner;
import com.marketplace.catalog.repository.*;
import com.marketplace.catalog.repository.impl.jdbc.*;
import com.marketplace.catalog.service.*;
import com.marketplace.catalog.service.api.AuditService;
import com.marketplace.catalog.service.api.AuthService;
import com.marketplace.catalog.service.api.ProductService;

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
        ProductRepository productRepository = new JdbcProductRepository();
        AuditRepository   auditRepository   = new JdbcAuditRepository();
        UserRepository    userRepository    = new JdbcUserRepository();

        // services
        Metrics metrics = new InMemoryMetrics();
        AuthService authService     = new AuthService(userRepository, auditRepository);
        ProductService productService = new ProductService(productRepository, auditRepository, metrics);
        AuditService auditService     = new AuditService(auditRepository);


        // console
        ConsoleApp app = new com.marketplace.catalog.console.ConsoleApp(
                authService, productService, auditService, metrics
        );
        app.run();
    }
}
