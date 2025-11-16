package com.marketplace.catalog.repository;

import com.marketplace.catalog.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для управления товарами в каталоге.
 */
public interface ProductRepository {

    /**
     * Создаёт новый или обновляет существующий товар.
     *
     * @param product товар для сохранения
     * @return сохранённый товар
     */
    Product save(Product product);

    /**
     * Ищет товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return Optional с товаром, если найден
     */
    Optional<Product> findById(Long id);

    /**
     * Возвращает все товары.
     *
     * @return список товаров
     */
    List<Product> findAll();

    /**
     * Удаляет товар по идентификатору.
     *
     * @param id идентификатор
     */
    void deleteById(Long id);

    /**
     * Возвращает количество товаров.
     *
     * @return количество товаров
     */
    long count();
}
