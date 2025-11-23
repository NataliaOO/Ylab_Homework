package com.marketplace.catalog.repository;

import com.marketplace.catalog.it.BasePgIT;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.repository.impl.jdbc.JdbcProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.marketplace.catalog.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class JdbcProductRepositoryIT extends BasePgIT {

    private ProductRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        truncate(TBL_PRODUCTS);
        repo = new JdbcProductRepository(connectionFactory, SCHEMA);
    }

    private Product newProduct(String name, BigDecimal price) {
        return new Product(
                null,
                name,
                BRAND_BIC,
                Category.CLOTHES,
                price,
                DESC_DEF
        );
    }

    @Test
    void create_shouldAssignId() {
        Product saved = repo.save(newProduct(NAME_PEN, PRICE_199));
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId()); // будет стабильно, если truncate делает RESTART IDENTITY
    }

    @Test
    void findById_shouldReturnSaved() {
        Product saved = repo.save(newProduct(NAME_PEN, PRICE_199));

        Product found = repo.findById(saved.getId()).orElseThrow();
        assertEquals(NAME_PEN, found.getName());
        assertEquals(BRAND_BIC, found.getBrand());
        assertEquals(Category.CLOTHES, found.getCategory());
        assertEquals(PRICE_199, found.getPrice());
        assertEquals(DESC_DEF, found.getDescription());
    }

    @Test
    void update_shouldModifyFields() {
        Product saved = repo.save(newProduct(NAME_PEN, PRICE_199));

        saved.setName(NAME_PEN_V2);
        saved.setPrice(PRICE_249);
        repo.save(saved);

        Product after = repo.findById(saved.getId()).orElseThrow();
        assertEquals(NAME_PEN_V2, after.getName());
        assertEquals(PRICE_249, after.getPrice());
    }

    @Test
    void count_shouldReflectRows() {
        assertEquals(0, repo.count());
        repo.save(newProduct("A", new BigDecimal("1.00")));
        repo.save(newProduct("B", new BigDecimal("2.00")));
        assertEquals(2, repo.count());
    }

    @Test
    void delete_shouldRemoveRow() {
        Product saved = repo.save(newProduct(NAME_PEN, PRICE_199));

        repo.deleteById(saved.getId());

        assertTrue(repo.findById(saved.getId()).isEmpty());
        assertEquals(0, repo.count());
    }
}
