package com.marketplace.catalog.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private String category;
    private BigDecimal price;
    private String description;
}
