package com.marketplace.catalog.service;

/**
 * In-memory реализация сервиса метрик.
 * Потокобезопасна за счёт synchronized.
 */
public class InMemoryMetrics implements Metrics {

    private long createCount;
    private long updateCount;
    private long deleteCount;

    private long searchCount;
    private long cacheHitCount;
    private long totalSearchTimeNanos;

    @Override
    public synchronized void recordCreate() { createCount++; }

    @Override
    public synchronized void recordUpdate() { updateCount++; }

    @Override
    public synchronized void recordDelete() { deleteCount++; }

    @Override
    public synchronized void recordSearch(long durationNanos, boolean fromCache) {
        searchCount++;
        totalSearchTimeNanos += durationNanos;
        if (fromCache) cacheHitCount++;
    }

    @Override
    public synchronized long getCreateCount() { return createCount; }

    @Override
    public synchronized long getUpdateCount() { return updateCount; }

    @Override
    public synchronized long getDeleteCount() { return deleteCount; }

    @Override
    public synchronized long getSearchCount() { return searchCount; }

    @Override
    public synchronized long getCacheHitCount() { return cacheHitCount; }

    @Override
    public synchronized double getAverageSearchTimeMillis() {
        if (searchCount == 0) return 0.0;
        return (totalSearchTimeNanos / 1_000_000.0) / searchCount;
    }

    @Override
    public synchronized double getCacheHitRatio() {
        if (searchCount == 0) return 0.0;
        return (double) cacheHitCount / searchCount;
    }
}
