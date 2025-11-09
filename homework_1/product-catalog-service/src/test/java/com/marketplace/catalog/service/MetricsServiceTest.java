package com.marketplace.catalog.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsServiceTest {

    @Test
    void recordCreateUpdateDelete_shouldIncreaseCounters() {
        MetricsService metrics = new MetricsService();

        metrics.recordCreate();
        metrics.recordCreate();
        metrics.recordUpdate();
        metrics.recordDelete();
        metrics.recordDelete();

        assertEquals(2, metrics.getCreateCount());
        assertEquals(1, metrics.getUpdateCount());
        assertEquals(2, metrics.getDeleteCount());
    }

    @Test
    void recordSearch_shouldUpdateSearchCountAndAverageTimeAndCacheHit() {
        MetricsService metrics = new MetricsService();

        metrics.recordSearch(1_000_000, false); // 1 ms
        metrics.recordSearch(3_000_000, true);  // 3 ms

        assertEquals(2, metrics.getSearchCount());
        assertEquals(1, metrics.getCacheHitCount());
        assertEquals(0.5, metrics.getCacheHitRatio());

        double avgMillis = metrics.getAverageSearchTimeMillis();
        assertTrue(avgMillis >= 1.0 && avgMillis <= 3.0,
                "Среднее время должно быть между 1 и 3 ms");
    }
}
