package com.marketplace.catalog.repository.impl;

import com.marketplace.catalog.model.Product;

import java.io.Serializable;
import java.util.List;

/**
 * Снимок состояния хранилища товаров для сериализации в файл.
 */
record DataSnapshot(List<Product> products) implements Serializable {
    private static final long serialVersionUID = 1L;
}
