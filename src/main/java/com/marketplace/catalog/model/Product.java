package com.marketplace.catalog.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Представляет товар в каталоге маркетплейса.
 * Содержит основную информацию о товаре.
 */
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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Product{" +
               "id=" + id +
               ", name='" + name + "'" +
               ", brand='" + brand + "'" +
               ", category=" + category +
               ", price=" + price +
               ", active=" + active +
               '}';
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
