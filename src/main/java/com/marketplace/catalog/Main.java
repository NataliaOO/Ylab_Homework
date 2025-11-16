package com.marketplace.catalog;

import com.marketplace.catalog.console.ConsoleApp;
import com.marketplace.catalog.repository.*;
import com.marketplace.catalog.repository.impl.FileAuditRepository;
import com.marketplace.catalog.repository.impl.FileProductRepository;
import com.marketplace.catalog.repository.impl.InMemoryUserRepository;
import com.marketplace.catalog.service.*;

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
        ProductRepository productRepository = new FileProductRepository("products.dat");
        AuditRepository auditRepository = new FileAuditRepository("audit.dat");
        UserRepository userRepository = new InMemoryUserRepository();

        Metrics metrics = new InMemoryMetrics();

        AuthService authService = new AuthService(userRepository, auditRepository);
        ProductService productService = new ProductService(productRepository, auditRepository, metrics);
        AuditService auditService = new AuditService(auditRepository);

        ConsoleApp app = new ConsoleApp(authService, productService, auditService, metrics);
        app.run();
    }
}
