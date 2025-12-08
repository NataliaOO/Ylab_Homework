package com.marketplace.catalog.it;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.config.Config;
import com.marketplace.catalog.db.ConnectionFactory;
import com.marketplace.catalog.db.LiquibaseRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
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
    protected static final String TBL_USERS    = SCHEMA + ".users";

    @Container
    protected static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>(PG_IMAGE)
                    .withDatabaseName(DB_NAME)
                    .withUsername(DB_USER)
                    .withPassword(DB_PASS);

    protected Config config;
    protected ConnectionFactory connectionFactory;

    @BeforeAll
    void initDb() throws Exception {
        System.setProperty("db.url", PG.getJdbcUrl());
        System.setProperty("db.user", PG.getUsername());
        System.setProperty("db.password", PG.getPassword());
        System.setProperty("db.schema", SCHEMA);

        Environment env = new StandardEnvironment();
        config = new AppConfig(env);
        connectionFactory = new ConnectionFactory(config);

        new LiquibaseRunner(config, connectionFactory).migrate();
    }

    protected void truncate(String table) throws Exception {
        try (Connection c = PG.createConnection(""); Statement st = c.createStatement()) {
            st.executeUpdate("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
        }
    }
}
