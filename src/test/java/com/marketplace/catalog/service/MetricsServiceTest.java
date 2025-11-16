package com.marketplace.catalog.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.marketplace.catalog.TestConstants.DELTA;
import static org.junit.jupiter.api.Assertions.*;

class MetricsServiceTest {

    private static final long ONE_MS_NANOS   = TimeUnit.MILLISECONDS.toNanos(1);
    private static final long THREE_MS_NANOS = TimeUnit.MILLISECONDS.toNanos(3);

    @Test
    void recordCreateUpdateDelete_shouldIncreaseCounters() {
        Metrics metrics = new InMemoryMetrics();

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
    void recordSearch_shouldUpdateSearchCountAverageTimeAndCacheHit() {
        Metrics metrics = new InMemoryMetrics();

        metrics.recordSearch(ONE_MS_NANOS, false);  // 1 ms
        metrics.recordSearch(THREE_MS_NANOS, true); // 3 ms

        assertEquals(2, metrics.getSearchCount());
        assertEquals(1, metrics.getCacheHitCount());
        assertEquals(0.5, metrics.getCacheHitRatio(), DELTA);

        assertEquals(2.0, metrics.getAverageSearchTimeMillis(), 0.001);
    }
}
