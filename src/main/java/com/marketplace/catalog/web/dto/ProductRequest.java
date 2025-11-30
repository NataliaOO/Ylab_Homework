package com.marketplace.catalog.web.dto;

import com.marketplace.catalog.model.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@AllArgsConstructor
public class ProductRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String brand;

    @NotNull
    private Category category;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    @Size(max = 2000)
    private String description;

    public ProductRequest() {
    }
}