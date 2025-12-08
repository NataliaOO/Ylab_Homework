package com.marketplace.catalog.db;

import com.marketplace.catalog.config.Config;
import com.marketplace.catalog.exception.MigrationException;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public final class LiquibaseRunner {
    private static final String PARAM_SCHEMA = "schema";
    private static final String DEFAULT_LIQUIBASE_SCHEMA = "public";

    private final Config config;
    private final ConnectionFactory connectionFactory;

    public LiquibaseRunner(Config config, ConnectionFactory connectionFactory) {
        this.config = config;
        this.connectionFactory = connectionFactory;
    }

    public void migrate() {
        String changelog = config.getLiquibaseChangelog();
        String schema    = config.getDbSchema();

        try (Connection conn = connectionFactory.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));

            database.setLiquibaseSchemaName(DEFAULT_LIQUIBASE_SCHEMA);
            database.setDefaultSchemaName(schema);

            Liquibase liquibase = new Liquibase(
                    changelog,
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.setChangeLogParameter(PARAM_SCHEMA, schema);

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            throw new MigrationException("Liquibase migration failed", e);
        }
    }
}
