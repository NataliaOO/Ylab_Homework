package com.marketplace.catalog.service;

/**
 * Контракт сервиса метрик каталога.
 */
public interface Metrics {
    void recordCreate();
    void recordUpdate();
    void recordDelete();

    /**
     * Регистрирует поисковый запрос.
     * @param durationNanos длительность поиска в наносекундах
     * @param fromCache     true, если результат пришёл из кэша
     */
    void recordSearch(long durationNanos, boolean fromCache);

    long getCreateCount();
    long getUpdateCount();
    long getDeleteCount();

    long getSearchCount();
    long getCacheHitCount();

    /**
     * Среднее время поиска в миллисекундах.
     */
    double getAverageSearchTimeMillis();

    /**
     * Доля запросов, обслуженных из кэша (0.0..1.0).
     */
    double getCacheHitRatio();
}
