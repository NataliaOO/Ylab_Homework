package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.Product;

import java.util.*;

/**
 * Простая in-memory реализация ProductRepository для тестов.
 */
public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> storage = new HashMap<>();
    private long sequence = 0L;

    @Override
    public synchronized Product save(Product product) {
        if (product.getId() == null) {
            product.setId(++sequence);
        } else if (product.getId() > sequence) {
            sequence = product.getId();
        }
        storage.put(product.getId(), product);
        return product;
    }

    @Override
    public synchronized Optional<Product> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public synchronized List<Product> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public synchronized long count() {
        return storage.size();
    }
}
