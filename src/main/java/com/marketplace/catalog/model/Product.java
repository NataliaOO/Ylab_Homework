package com.marketplace.catalog.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Представляет товар в каталоге маркетплейса.
 * Содержит основную информацию о товаре.
 */
@Getter
@Setter
@ToString
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Уникальный идентификатор товара. */
    private Long id;
    /** Название товара. */
    private String name;
    /** Бренд товара. */
    private String brand;
    /** Категория товара. */
    private Category category;
    /** Цена товара. */
    private BigDecimal price;
    /** Описание товара. */
    private String description;
    /** Признак активности товара. */
    private boolean active = true;

    /**
     * Создаёт новый товар.
     *
     * @param id          идентификатор товара (может быть null для нового)
     * @param name        название товара
     * @param brand       бренд товара
     * @param category    категория товара
     * @param price       цена товара
     * @param description описание товара
     */
    public Product(Long id,
                   String name,
                   String brand,
                   Category category,
                   BigDecimal price,
                   String description) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        // Если есть id — сравниваем по id, иначе — по ссылочной идентичности
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
