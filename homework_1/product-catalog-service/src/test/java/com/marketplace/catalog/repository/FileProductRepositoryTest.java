package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileProductRepositoryTest {

    private final String fileName = "test-products.dat";

    @AfterEach
    void tearDown() {
        File f = new File(fileName);
        if (f.exists()) {
            assertTrue(f.delete(), "Не удалось удалить файл тестовых данных");
        }
    }

    @Test
    void saveAndReload_shouldPersistProductsBetweenInstances() {
        FileProductRepository repo1 = new FileProductRepository(fileName);

        Product p1 = new Product(null, "Test1", "Brand1", Category.BOOKS,
                new BigDecimal("10.00"), "Desc1");
        Product p2 = new Product(null, "Test2", "Brand2", Category.ELECTRONICS,
                new BigDecimal("20.00"), "Desc2");

        repo1.save(p1);
        repo1.save(p2);

        assertEquals(2, repo1.count());

        FileProductRepository repo2 = new FileProductRepository(fileName);
        List<Product> loaded = repo2.findAll();

        assertEquals(2, loaded.size());
        assertTrue(loaded.stream().anyMatch(p -> "Test1".equals(p.getName())));
        assertTrue(loaded.stream().anyMatch(p -> "Test2".equals(p.getName())));
    }
}
