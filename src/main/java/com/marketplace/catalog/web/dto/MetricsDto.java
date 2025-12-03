package com.marketplace.catalog.web.dto;

/**
 * DTO для метрик каталога.
 */
public record MetricsDto(
        long createCount,
        long updateCount,
        long deleteCount,
        long searchCount,
        long cacheHitCount,
        double averageSearchTimeMillis,
        double cacheHitRatio
) {}
