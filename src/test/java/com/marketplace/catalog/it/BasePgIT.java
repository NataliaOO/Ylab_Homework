package com.marketplace.catalog.it;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.Statement;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BasePgIT {

    protected static final String PG_IMAGE = "postgres:16";
    protected static final String DB_NAME  = "testdb";
    protected static final String DB_USER  = "testuser";
    protected static final String DB_PASS  = "testpass";

    protected static final String SCHEMA = "catalog";
    protected static final String TBL_PRODUCTS = SCHEMA + ".product";
    protected static final String TBL_AUDIT    = SCHEMA + ".audit";
    protected static final String TBL_USERS    = SCHEMA + ".users";

    @Container
    protected static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>(PG_IMAGE)
                    .withDatabaseName(DB_NAME)
                    .withUsername(DB_USER)
                    .withPassword(DB_PASS);

    @BeforeAll
    void initDb() throws Exception {
        System.setProperty("db.url", PG.getJdbcUrl());
        System.setProperty("db.user", PG.getUsername());
        System.setProperty("db.password", PG.getPassword());
        System.setProperty("db.schema", SCHEMA);

        try (Connection c = PG.createConnection(""); Statement st = c.createStatement()) {
            st.addBatch("CREATE SCHEMA IF NOT EXISTS " + SCHEMA + ";");

            st.addBatch("""
                CREATE TABLE IF NOT EXISTS %s (
                  id BIGSERIAL PRIMARY KEY,
                  name VARCHAR(255) NOT NULL,
                  brand VARCHAR(255),
                  category VARCHAR(64),
                  price NUMERIC(19,2),
                  description TEXT
                );
            """.formatted(TBL_PRODUCTS));

            st.addBatch("""
                CREATE TABLE IF NOT EXISTS %s (
                  id BIGSERIAL PRIMARY KEY,
                  created_at TIMESTAMP NOT NULL,
                  username VARCHAR(128),
                  action VARCHAR(128),
                  details TEXT
                );
            """.formatted(TBL_AUDIT));

            st.addBatch("""
                CREATE TABLE IF NOT EXISTS %s (
                  id BIGSERIAL PRIMARY KEY,
                  login VARCHAR(128) NOT NULL UNIQUE,
                  password VARCHAR(256) NOT NULL,
                  role VARCHAR(32) NOT NULL
                );
            """.formatted(TBL_USERS));

            st.executeBatch();

            // Тестовые пользователи
            st.addBatch("""
                INSERT INTO %s (login, password, role)
                VALUES ('admin', 'admin', 'ADMIN')
                ON CONFLICT (login) DO NOTHING;
            """.formatted(TBL_USERS));
            st.addBatch("""
                INSERT INTO %s (login, password, role)
                VALUES ('user', 'user', 'VIEWER')
                ON CONFLICT (login) DO NOTHING;
            """.formatted(TBL_USERS));
            st.executeBatch();
        }
    }

    protected void truncate(String table) throws Exception {
        try (Connection c = PG.createConnection(""); Statement st = c.createStatement()) {
            st.executeUpdate("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
        }
    }
}
