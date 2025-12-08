package com.marketplace.catalog;

import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.web.dto.ProductDto;
import com.marketplace.catalog.web.dto.ProductRequest;

import java.math.BigDecimal;

public final class TestData {
    private TestData() {}

    public static final String BRAND_BIC = "Bic";
    public static final String NAME_PEN = "Pen";
    public static final String NAME_PEN_V2 = "Pen v2";
    public static final String DESC_DEF = "desc";

    public static final BigDecimal PRICE_199 = new BigDecimal("1.99");
    public static final BigDecimal PRICE_249 = new BigDecimal("2.49");

    // Double сравнение
    public static final double DELTA = 1e-9;

    public static User adminUser() {
        return new User(1L, "admin", "encoded-password", Role.ADMIN);
    }

    public static User viewerUser() {
        return new User(2L, "viewer", "encoded-password", Role.VIEWER);
    }

    /** Product Pen v2 с ценой 2.49 */
    public static Product productPenV2(Long id) {
        return new Product(
                id,
                NAME_PEN_V2,
                DESC_DEF,
                Category.HOME,
                PRICE_249,
                BRAND_BIC
        );
    }

    /** Запрос на обновление Pen → Pen v2 по цене 2.49 */
    public static ProductRequest productRequestPenV2_249() {
        return new ProductRequest(
                NAME_PEN_V2,
                DESC_DEF,
                Category.HOME,
                PRICE_249,
                BRAND_BIC
        );
    }

    /** Готовый ProductDto для Pen v2 2.49 */
    public static ProductDto productDtoPenV2_249(Long id) {
        return new ProductDto(
                id,
                NAME_PEN_V2,
                DESC_DEF,
                Category.HOME.name(),
                PRICE_249,
                BRAND_BIC
        );
    }
}
