package com.marketplace.catalog.service;

import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private AuditRepository auditRepository;
    private MetricsService metricsService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
        auditRepository = new InMemoryAuditRepository();
        metricsService = new MetricsService();
        productService = new ProductService(productRepository, auditRepository, metricsService);

        productService.createProduct(
                new Product(null, "iPhone 15", "Apple", Category.ELECTRONICS,
                        new BigDecimal("999.99"), "Smartphone"),
                "admin"
        );
        productService.createProduct(
                new Product(null, "Galaxy S24", "Samsung", Category.ELECTRONICS,
                        new BigDecimal("899.99"), "Smartphone"),
                "admin"
        );
        productService.createProduct(
                new Product(null, "The Witcher", "AST", Category.BOOKS,
                        new BigDecimal("19.99"), "Fantasy book"),
                "admin"
        );
    }

    @Test
    void createProduct_shouldIncreaseCountAndWriteMetrics() {
        long beforeCount = productService.count();

        Product p = new Product(null, "MacBook", "Apple", Category.ELECTRONICS,
                new BigDecimal("1999.99"), "Laptop");
        Product created = productService.createProduct(p, "admin");

        assertNotNull(created.getId());
        assertEquals(beforeCount + 1, productService.count());
        assertEquals(4, metricsService.getCreateCount());
    }

    @Test
    void updateProduct_shouldModifyExistingProduct() {
        List<Product> all = productService.findAll();
        Product first = all.get(0);

        Product updated = new Product(null, "iPhone 15 Pro", "Apple", Category.ELECTRONICS,
                new BigDecimal("1299.99"), "Flagship");

        Optional<Product> result = productService.updateProduct(first.getId(), updated, "admin");

        assertTrue(result.isPresent());
        Product p = result.get();
        assertEquals("iPhone 15 Pro", p.getName());
        assertEquals(new BigDecimal("1299.99"), p.getPrice());
        assertEquals(1, metricsService.getUpdateCount());
    }

    @Test
    void updateProduct_nonExistingId_shouldReturnEmpty() {
        Optional<Product> result = productService.updateProduct(999L,
                new Product(null, "Test", "Brand", Category.BOOKS, BigDecimal.ONE, "Desc"),
                "admin");

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteProduct_shouldRemoveProductAndIncreaseMetrics() {
        List<Product> all = productService.findAll();
        Product first = all.get(0);

        boolean deleted = productService.deleteProduct(first.getId(), "admin");

        assertTrue(deleted);
        assertEquals(2, productService.count());
        assertEquals(1, metricsService.getDeleteCount());
    }

    @Test
    void deleteProduct_nonExistingId_shouldReturnFalse() {
        boolean deleted = productService.deleteProduct(999L, "admin");
        assertFalse(deleted);
        assertEquals(0, metricsService.getDeleteCount());
    }

    @Test
    void search_shouldFilterByCategoryAndBrandAndPrice() {
        List<Product> result = productService.search(
                Category.ELECTRONICS,
                "Apple",
                new BigDecimal("900"),
                null,
                null
        );

        assertEquals(1, result.size());
        assertEquals("Apple", result.get(0).getBrand());
        assertEquals(Category.ELECTRONICS, result.get(0).getCategory());
    }

    @Test
    void search_shouldFindByTextInNameOrDescription() {
        List<Product> result = productService.search(
                null, null, null, null, "witcher"
        );

        assertEquals(1, result.size());
        assertEquals("The Witcher", result.get(0).getName());
    }

    @Test
    void search_shouldUseCacheAndUpdateMetrics() {
        List<Product> result1 = productService.search(
                Category.ELECTRONICS, null, null, null, null
        );
        assertFalse(result1.isEmpty());

        List<Product> result2 = productService.search(
                Category.ELECTRONICS, null, null, null, null
        );
        assertEquals(result1.size(), result2.size());

        assertEquals(2, metricsService.getSearchCount());
        assertEquals(1, metricsService.getCacheHitCount());
        assertTrue(metricsService.getAverageSearchTimeMillis() >= 0.0);
    }
}
