package com.marketplace.catalog.console;

import com.marketplace.catalog.exception.ProductValidationException;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.service.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Консольное приложение для работы с каталогом товаров.
 */
public class ConsoleApp {

    private final AuthService authService;
    private final ProductService productService;
    private final AuditService auditService;
    private final MetricsService metricsService;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleApp(AuthService authService,
                      ProductService productService,
                      AuditService auditService,
                      MetricsService metricsService) {
        this.authService = authService;
        this.productService = productService;
        this.auditService = auditService;
        this.metricsService = metricsService;
    }

    /**
     * Запускает основной цикл работы приложения.
     */
    public void run() {
        System.out.println("=== Product Catalog Service ===");
        if (!login()) {
            System.out.println("Не удалось войти. Завершение.");
            return;
        }

        boolean exit = false;
        while (!exit) {
            printMenu();
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> addProduct();
                    case "2" -> editProduct();
                    case "3" -> deleteProduct();
                    case "4" -> listProducts();
                    case "5" -> searchProducts();
                    case "6" -> showAudit();
                    case "7" -> showMetrics();
                    case "0" -> {
                        authService.logout();
                        exit = true;
                    }
                    default -> System.out.println("Неизвестная команда");
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        System.out.println("До встречи!");
    }

    private boolean login() {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        boolean ok = authService.login(login, password);
        if (!ok) {
            System.out.println("Неверные данные.");
        }
        return ok;
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Текущий пользователь: " +
                (authService.getCurrentUser() != null
                        ? authService.getCurrentUser().getLogin() + " (" + authService.getCurrentUser().getRole() + ")"
                        : "нет"));
        System.out.println("Всего товаров: " + productService.count());
        System.out.println("1. Добавить товар (ADMIN)");
        System.out.println("2. Изменить товар (ADMIN)");
        System.out.println("3. Удалить товар (ADMIN)");
        System.out.println("4. Показать все товары");
        System.out.println("5. Поиск/фильтрация");
        System.out.println("6. Показать аудит");
        System.out.println("7. Показать метрики");
        System.out.println("0. Выход");
        System.out.print("Выберите пункт меню: ");
    }

    private void addProduct() {
        if (!authService.isAdmin()) {
            System.out.println("Недостаточно прав.");
            return;
        }
        System.out.println("=== Добавление товара ===");
        System.out.print("Название: ");
        String name = scanner.nextLine();
        System.out.print("Бренд: ");
        String brand = scanner.nextLine();
        System.out.print("Категория (ELECTRONICS/CLOTHES/BOOKS/HOME/BEAUTY): ");
        Category category = Category.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Цена: ");
        BigDecimal price = new BigDecimal(scanner.nextLine());
        System.out.print("Описание: ");
        String desc = scanner.nextLine();

        Product p = new Product(null, name, brand, category, price, desc);
        productService.createProduct(p, authService.getCurrentUser().getLogin());
        System.out.println("Товар добавлен.");
    }

    private void editProduct() {
        if (!authService.isAdmin()) {
            System.out.println("Недостаточно прав.");
            return;
        }
        System.out.print("ID товара для изменения: ");
        Long id = Long.parseLong(scanner.nextLine());
        System.out.print("Новое название: ");
        String name = scanner.nextLine();
        System.out.print("Новый бренд: ");
        String brand = scanner.nextLine();
        System.out.print("Новая категория (ELECTRONICS/CLOTHES/BOOKS/HOME/BEAUTY): ");
        Category category = Category.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Новая цена: ");
        BigDecimal price = new BigDecimal(scanner.nextLine());
        System.out.print("Новое описание: ");
        String desc = scanner.nextLine();

        Product updated = new Product(null, name, brand, category, price, desc);
        productService.updateProduct(id, updated, authService.getCurrentUser().getLogin())
                .ifPresentOrElse(
                        p -> System.out.println("Товар обновлён: " + p),
                        () -> System.out.println("Товар с таким ID не найден.")
                );
    }

    private void deleteProduct() {
        if (!authService.isAdmin()) {
            System.out.println("Недостаточно прав.");
            return;
        }
        System.out.print("ID товара для удаления: ");
        Long id = Long.parseLong(scanner.nextLine());
        boolean ok = productService.deleteProduct(id, authService.getCurrentUser().getLogin());
        System.out.println(ok ? "Удалено." : "Товар не найден.");
    }

    private void listProducts() {
        System.out.println("=== Все товары ===");
        productService.findAll().forEach(System.out::println);
    }

    private void searchProducts() {
        System.out.println("=== Поиск ===");
        System.out.print("Категория (ENTER - пропустить): ");
        String catInput = scanner.nextLine();
        Category category = catInput.isBlank() ? null : Category.valueOf(catInput.trim().toUpperCase());

        System.out.print("Бренд (ENTER - пропустить): ");
        String brand = scanner.nextLine();
        if (brand.isBlank()) {
            brand = null;
        }

        System.out.print("Мин. цена (ENTER - пропустить): ");
        String minStr = scanner.nextLine();
        BigDecimal min = minStr.isBlank() ? null : new BigDecimal(minStr);

        System.out.print("Макс. цена (ENTER - пропустить): ");
        String maxStr = scanner.nextLine();
        BigDecimal max = maxStr.isBlank() ? null : new BigDecimal(maxStr);

        System.out.print("Текст в названии/описании (ENTER - пропустить): ");
        String text = scanner.nextLine();
        if (text.isBlank()) {
            text = null;
        }

        List<Product> result = productService.search(category, brand, min, max, text);
        System.out.println("=== Результаты ===");
        result.forEach(System.out::println);
    }

    private void showAudit() {
        System.out.println("=== Аудит ===");
        auditService.findAll().forEach(System.out::println);
    }

    private void showMetrics() {
        System.out.println("=== Метрики ===");
        System.out.println("Товаров в каталоге: " + productService.count());
        System.out.println("Создано товаров: " + metricsService.getCreateCount());
        System.out.println("Обновлено товаров: " + metricsService.getUpdateCount());
        System.out.println("Удалено товаров: " + metricsService.getDeleteCount());
        System.out.println("Поисковых запросов: " + metricsService.getSearchCount());
        System.out.println("Попаданий в кэш: " + metricsService.getCacheHitCount());
        System.out.printf("Среднее время поиска: %.3f ms%n", metricsService.getAverageSearchTimeMillis());
        System.out.printf("Доля запросов из кэша: %.2f%n", metricsService.getCacheHitRatio());
    }
}
