package com.marketplace.catalog.service;

import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import java.math.BigDecimal;
import java.util.*;


/**
 * Контракт сервиса для работы с каталогом товаров.
 */
public interface ProductService {

    Product createProduct(Product product, String username);

    Optional<Product> updateProduct(Long id, Product updated, String username);

    boolean deleteProduct(Long id, String username);

    List<Product> findAll();

    long count();

    List<Product> search(Category category,
                         String brand,
                         BigDecimal minPrice,
                         BigDecimal maxPrice,
                         String text);
}
