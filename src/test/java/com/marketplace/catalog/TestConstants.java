package com.marketplace.catalog;

import java.math.BigDecimal;

public final class TestConstants {
    private TestConstants() {}

    // Доменные значения
    public static final String BRAND_BIC = "Bic";
    public static final String NAME_PEN = "Pen";
    public static final String NAME_PEN_V2 = "Pen v2";
    public static final String DESC_DEF = "desc";

    public static final BigDecimal PRICE_199 = new BigDecimal("1.99");
    public static final BigDecimal PRICE_249 = new BigDecimal("2.49");

    // Double сравнение
    public static final double DELTA = 1e-9;
}
