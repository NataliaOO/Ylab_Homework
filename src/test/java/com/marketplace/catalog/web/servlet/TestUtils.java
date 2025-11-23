package com.marketplace.catalog.web.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Вспомогательные методы для тестов сервлетов.
 */
public final class TestUtils {

    private TestUtils() {
        // утильный класс - не инстанцируем
    }

    public static ServletInputStream toServletInputStream(String body) {
        byte[] bytes = body != null
                ? body.getBytes(StandardCharsets.UTF_8)
                : new byte[0];

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return bais.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // no-op
            }
        };
    }
}
