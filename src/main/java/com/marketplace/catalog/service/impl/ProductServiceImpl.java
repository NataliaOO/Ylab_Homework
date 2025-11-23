package com.marketplace.catalog.service.impl;

import com.marketplace.catalog.exception.ProductValidationException;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.service.Metrics;
import com.marketplace.catalog.service.ProductService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductServiceImpl  implements ProductService {

    private final ProductRepository productRepository;
    private final Metrics metrics;

    private final Map<String, List<Product>> searchCache = new HashMap<>();

    public ProductServiceImpl(ProductRepository productRepository,
                          Metrics metrics) {
        this.productRepository = productRepository;
        this.metrics = metrics;
    }

    /**
     * Создаёт новый товар.
     */
    public Product createProduct(Product product, String username) {
        validateProduct(product, username);
        Product saved = productRepository.save(product);
        invalidateCache();
        metrics.recordCreate();
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
        metrics.recordUpdate();
        return Optional.of(existing);
    }

    /**
     * Удаляет товар.
     */
    public boolean deleteProduct(Long id, String username) {
        if (productRepository.findById(id).isPresent()) {
            productRepository.deleteById(id);
            invalidateCache();
            metrics.recordDelete();
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
        long started = System.nanoTime();
        String key = buildCacheKey(category, brand, minPrice, maxPrice, text);

        List<Product> result = getFromCache(key);
        boolean fromCache = result != null;

        if (!fromCache) {
            result = filterProducts(category, brand, minPrice, maxPrice, text);
            putToCache(key, result);
        }

        metrics.recordSearch(System.nanoTime() - started, fromCache);
        return result;
    }

    private List<Product> getFromCache(String key) {
        return searchCache.get(key);
    }

    private void putToCache(String key, List<Product> value) {
        searchCache.put(key, value);
    }

    /** Применяет все фильтры к списку товаров. */
    private List<Product> filterProducts(Category category,
                                         String brand,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice,
                                         String text) {
        final String brandNorm = norm(brand);
        final String textNorm = norm(text);

        return productRepository.findAll().stream()
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> brandNorm == null || norm(p.getBrand()) != null && norm(p.getBrand()).equals(brandNorm))
                .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .filter(p -> textNorm == null || containsText(p, textNorm))
                .collect(Collectors.toList());
    }

    /** Нормализует строку для сравнения (lowercase + trim); null/blank → null. */
    private String norm(String s) {
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        return t.isBlank() ? null : t;
    }

    /** Проверка вхождения текста в name/description. */
    private boolean containsText(Product p, String textNorm) {
        String name = norm(p.getName());
        String desc = norm(p.getDescription());
        return (name != null && name.contains(textNorm)) ||
                (desc != null && desc.contains(textNorm));
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

    private void invalidateCache() {
        searchCache.clear();
    }

    /**
     * Валидация товара.
     * Логирует ошибку в аудит и выбрасывает исключение при некорректных данных.
     */
    private void validateProduct(Product product, String username) {
        validateName(product, username);
        validateCategory(product, username);
        validatePricePresent(product, username);
        validatePriceNonNegative(product, username);
    }

    /** Имя товара обязательно и не может быть пустым. */
    private void validateName(Product product, String username) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new ProductValidationException("Название товара не может быть пустым");
        }
    }

    /** Категория товара обязательна. */
    private void validateCategory(Product product, String username) {
        if (product.getCategory() == null) {
            throw new ProductValidationException("Категория товара должна быть указана");
        }
    }

    /** Цена должна быть указана (не null). */
    private void validatePricePresent(Product product, String username) {
        if (product.getPrice() == null) {
            throw new ProductValidationException("Цена товара должна быть указана");
        }
    }

    /** Цена не может быть отрицательной. */
    private void validatePriceNonNegative(Product product, String username) {
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductValidationException("Цена товара не может быть отрицательной");
        }
    }
}
