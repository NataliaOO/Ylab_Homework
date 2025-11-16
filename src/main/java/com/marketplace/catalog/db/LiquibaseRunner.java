package com.marketplace.catalog.db;

import com.marketplace.catalog.config.AppConfig;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public final class LiquibaseRunner {

    private LiquibaseRunner() { }

    public static void migrate() {
        String changelog = AppConfig.get("liquibase.changelog");
        String schema = AppConfig.get("db.schema");

        try (Connection conn = ConnectionFactory.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));

            // доменная схема (таблицы сущностей)
            database.setDefaultSchemaName(schema);

            Liquibase liquibase = new Liquibase(
                    changelog,
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.setChangeLogParameter("schema", schema);

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}
