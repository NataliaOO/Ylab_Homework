package com.marketplace.catalog.service;

import com.marketplace.catalog.exception.ProductValidationException;
import com.marketplace.catalog.model.AuditRecord;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.repository.AuditRepository;
import com.marketplace.catalog.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для работы с каталогом товаров.
 */
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditRepository auditRepository;
    private final MetricsService metricsService;

    private final Map<String, List<Product>> searchCache = new HashMap<>();

    public ProductService(ProductRepository productRepository,
                          AuditRepository auditRepository,
                          MetricsService metricsService) {
        this.productRepository = productRepository;
        this.auditRepository = auditRepository;
        this.metricsService = metricsService;
    }

    private void invalidateCache() {
        searchCache.clear();
    }

    /**
     * Валидация товара.
     * Логирует ошибку в аудит и выбрасывает исключение при некорректных данных.
     */
    private void validateProduct(Product product, String username) {
        if (product.getName() == null || product.getName().isBlank()) {
            String msg = "Название товара не может быть пустым";
            auditRepository.save(new AuditRecord(
                    LocalDateTime.now(),
                    username,
                    "PRODUCT_VALIDATION_ERROR",
                    msg
            ));
            throw new ProductValidationException(msg);
        }

        if (product.getPrice() == null) {
            String msg = "Цена товара должна быть указана";
            auditRepository.save(new AuditRecord(
                    LocalDateTime.now(),
                    username,
                    "PRODUCT_VALIDATION_ERROR",
                    msg
            ));
            throw new ProductValidationException(msg);
        }

        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            String msg = "Цена товара не может быть отрицательной";
            auditRepository.save(new AuditRecord(
                    LocalDateTime.now(),
                    username,
                    "PRODUCT_VALIDATION_ERROR",
                    msg
            ));
            throw new ProductValidationException(msg);
        }
    }

    /**
     * Создаёт новый товар.
     */
    public Product createProduct(Product product, String username) {
        validateProduct(product, username);

        Product saved = productRepository.save(product);
        invalidateCache();
        auditRepository.save(new AuditRecord(LocalDateTime.now(), username,
                "CREATE_PRODUCT", "id=" + saved.getId()));
        metricsService.recordCreate();
        return saved;
    }

    /**
     * Обновляет существующий товар.
     */
    public Optional<Product> updateProduct(Long id, Product updated, String username) {
        Optional<Product> existingOpt = productRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        Product existing = existingOpt.get();
        existing.setName(updated.getName());
        existing.setBrand(updated.getBrand());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setDescription(updated.getDescription());

        validateProduct(updated, username);

        productRepository.save(existing);
        invalidateCache();
        auditRepository.save(new AuditRecord(LocalDateTime.now(), username,
                "UPDATE_PRODUCT", "id=" + id));
        metricsService.recordUpdate();
        return Optional.of(existing);
    }

    /**
     * Удаляет товар.
     */
    public boolean deleteProduct(Long id, String username) {
        if (productRepository.findById(id).isPresent()) {
            productRepository.deleteById(id);
            invalidateCache();
            auditRepository.save(new AuditRecord(LocalDateTime.now(), username,
                    "DELETE_PRODUCT", "id=" + id));
            metricsService.recordDelete();
            return true;
        }
        return false;
    }

    /**
     * Возвращает все товары.
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * Возвращает количество товаров.
     */
    public long count() {
        return productRepository.count();
    }

    /**
     * Поиск и фильтрация товаров.
     */
    public List<Product> search(Category category,
                                String brand,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                String text) {

        long start = System.nanoTime();
        String cacheKey = buildCacheKey(category, brand, minPrice, maxPrice, text);
        boolean fromCache = false;
        List<Product> result;

        if (searchCache.containsKey(cacheKey)) {
            result = searchCache.get(cacheKey);
            fromCache = true;
            System.out.println(">> Результат взят из кэша. Найдено: " + result.size());
        } else {
            result = productRepository.findAll().stream()
                    .filter(p -> category == null || p.getCategory() == category)
                    .filter(p -> brand == null || p.getBrand().equalsIgnoreCase(brand))
                    .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                    .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                    .filter(p -> text == null ||
                            p.getName().toLowerCase().contains(text.toLowerCase()) ||
                            (p.getDescription() != null &&
                             p.getDescription().toLowerCase().contains(text.toLowerCase())))
                    .collect(Collectors.toList());

            searchCache.put(cacheKey, result);
        }

        long end = System.nanoTime();
        long duration = end - start;
        metricsService.recordSearch(duration, fromCache);

        System.out.println(">> Запрос выполнен за " + (duration / 1_000_000) + " ms. " +
                "Найдено: " + result.size());

        return result;
    }

    private String buildCacheKey(Category category, String brand,
                                 BigDecimal minPrice, BigDecimal maxPrice,
                                 String text) {
        return (category != null ? category.name() : "_") + "|" +
               (brand != null ? brand.toLowerCase() : "_") + "|" +
               (minPrice != null ? minPrice : "_") + "|" +
               (maxPrice != null ? maxPrice : "_") + "|" +
               (text != null ? text.toLowerCase() : "_");
    }
}
