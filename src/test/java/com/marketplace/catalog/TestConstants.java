package com.marketplace.catalog;

import java.util.concurrent.TimeUnit;

public final class TestConstants {
    private TestConstants() {}

    public static final long ONE_MS_NANOS   = TimeUnit.MILLISECONDS.toNanos(1);
    public static final long THREE_MS_NANOS = TimeUnit.MILLISECONDS.toNanos(3);
    public static final double DELTA = 1e-9;

    public static final String ADMIN_LOGIN = "admin";
    public static final String ADMIN_PASS  = "admin";
    public static final String USER_LOGIN  = "user";
    public static final String USER_PASS   = "user";
}
