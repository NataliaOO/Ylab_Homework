package com.marketplace.catalog.it;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BasePgIT {

    protected static final String PG_IMAGE = "postgres:16";
    protected static final String DB_NAME  = "testdb";
    protected static final String DB_USER  = "testuser";
    protected static final String DB_PASS  = "testpass";

    // Доменные схемы/объекты (НЕ public)
    protected static final String SCHEMA        = "catalog";
    protected static final String TBL_PRODUCTS  = SCHEMA + ".products";
    protected static final String TBL_AUDIT     = SCHEMA + ".audit";
    protected static final String TBL_USERS     = SCHEMA + ".users";
    protected static final String SEQ_PRODUCT   = SCHEMA + ".product_seq";
    protected static final String SEQ_AUDIT     = SCHEMA + ".audit_seq";
    protected static final String SEQ_USER      = SCHEMA + ".user_seq";

    protected PostgreSQLContainer<?> pg;

    @BeforeAll
    void startPg() throws Exception {
        pg = new PostgreSQLContainer<>(PG_IMAGE)
                .withDatabaseName(DB_NAME)
                .withUsername(DB_USER)
                .withPassword(DB_PASS);
        pg.start();

        // Переопределяем конфиг приложения значениями контейнера
        System.setProperty("db.url", pg.getJdbcUrl());
        System.setProperty("db.user", pg.getUsername());
        System.setProperty("db.password", pg.getPassword());
        System.setProperty("db.schema", SCHEMA);

        // DDL (минимум для users/products/audit + sequences)
        try (Connection c = pg.createConnection(""); Statement st = c.createStatement()) {
            st.addBatch("CREATE SCHEMA IF NOT EXISTS " + SCHEMA + ";");

            st.addBatch("CREATE SEQUENCE IF NOT EXISTS " + SEQ_PRODUCT + " START 1 INCREMENT 1;");
            st.addBatch("CREATE SEQUENCE IF NOT EXISTS " + SEQ_AUDIT   + " START 1 INCREMENT 1;");
            st.addBatch("CREATE SEQUENCE IF NOT EXISTS " + SEQ_USER    + " START 1 INCREMENT 1;");

            st.addBatch("""
                CREATE TABLE IF NOT EXISTS %s (
                  id BIGINT PRIMARY KEY,
                  name VARCHAR(255) NOT NULL,
                  brand VARCHAR(255),
                  category VARCHAR(64),
                  price NUMERIC(19,2),
                  description TEXT,
                  active BOOLEAN DEFAULT TRUE
                );
            """.formatted(TBL_PRODUCTS));

            st.addBatch("""
                CREATE TABLE IF NOT EXISTS %s (
                  id BIGINT PRIMARY KEY,
                  ts TIMESTAMP NOT NULL,
                  username VARCHAR(128),
                  action VARCHAR(128),
                  details TEXT
                );
            """.formatted(TBL_AUDIT));

            st.addBatch("""
                CREATE TABLE IF NOT EXISTS %s (
                  id BIGINT PRIMARY KEY,
                  login VARCHAR(128) NOT NULL UNIQUE,
                  password VARCHAR(256) NOT NULL,
                  role VARCHAR(32) NOT NULL
                );
            """.formatted(TBL_USERS));

            st.executeBatch();

            // Тестовые пользователи
            st.addBatch("""
                INSERT INTO %s (id, login, password, role)
                VALUES (nextval('%s'), 'admin', 'admin', 'ADMIN')
                ON CONFLICT (login) DO NOTHING;
            """.formatted(TBL_USERS, SEQ_USER));
            st.addBatch("""
                INSERT INTO %s (id, login, password, role)
                VALUES (nextval('%s'), 'user', 'user', 'VIEWER')
                ON CONFLICT (login) DO NOTHING;
            """.formatted(TBL_USERS, SEQ_USER));
            st.executeBatch();
        }
    }

    @AfterAll
    void stopPg() {
        if (pg != null) pg.stop();
    }

    /** Выполнить SQL без результата. Упрощённый helper для DDL/DML. */
    protected void exec(String sql) throws Exception {
        try (Connection c = pg.createConnection(""); Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

    /** Очистить таблицу и сбросить sequence. */
    protected void truncateAndRestartSeq(String table, String seq) throws Exception {
        try (Connection c = pg.createConnection(""); Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM " + table);
            st.executeUpdate("ALTER SEQUENCE " + seq + " RESTART WITH 1");
        }
    }
}
