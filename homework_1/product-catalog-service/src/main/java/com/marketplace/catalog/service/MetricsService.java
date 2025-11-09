package com.marketplace.catalog.service;

/**
 * Сервис для сбора простых метрик по работе каталога.
 */
public class MetricsService {

    private long createCount;
    private long updateCount;
    private long deleteCount;

    private long searchCount;
    private long cacheHitCount;
    private long totalSearchTimeNanos;

    /**
     * Регистрирует операцию создания товара.
     */
    public synchronized void recordCreate() {
        createCount++;
    }

    /**
     * Регистрирует операцию обновления товара.
     */
    public synchronized void recordUpdate() {
        updateCount++;
    }

    /**
     * Регистрирует операцию удаления товара.
     */
    public synchronized void recordDelete() {
        deleteCount++;
    }

    /**
     * Регистрирует поисковый запрос.
     *
     * @param durationNanos длительность выполнения запроса в наносекундах
     * @param fromCache     true, если результат получен из кэша
     */
    public synchronized void recordSearch(long durationNanos, boolean fromCache) {
        searchCount++;
        totalSearchTimeNanos += durationNanos;
        if (fromCache) {
            cacheHitCount++;
        }
    }

    public synchronized long getCreateCount() {
        return createCount;
    }

    public synchronized long getUpdateCount() {
        return updateCount;
    }

    public synchronized long getDeleteCount() {
        return deleteCount;
    }

    public synchronized long getSearchCount() {
        return searchCount;
    }

    public synchronized long getCacheHitCount() {
        return cacheHitCount;
    }

    /**
     * Возвращает среднее время выполнения поиска в миллисекундах.
     */
    public synchronized double getAverageSearchTimeMillis() {
        if (searchCount == 0) return 0.0;
        return (totalSearchTimeNanos / 1_000_000.0) / searchCount;
    }

    /**
     * Возвращает долю запросов, обслуженных из кэша.
     */
    public synchronized double getCacheHitRatio() {
        if (searchCount == 0) return 0.0;
        return (double) cacheHitCount / searchCount;
    }
}
