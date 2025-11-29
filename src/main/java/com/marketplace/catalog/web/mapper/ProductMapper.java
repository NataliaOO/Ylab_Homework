package com.marketplace.catalog.web.mapper;

import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.web.dto.ProductDto;
import com.marketplace.catalog.web.dto.ProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "default")
public interface ProductMapper {

    ProductDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", expression = "java(mapCategory(request.getCategory()))")
    Product fromRequest(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", expression = "java(mapCategory(request.getCategory()))")
    void updateProduct(@MappingTarget Product product, ProductRequest request);

    default Category mapCategory(String name) {
        if (name == null) {
            return null;
        }
        return Category.valueOf(name.toUpperCase());
    }
}
