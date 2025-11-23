package com.marketplace.catalog.web.listener;

import com.marketplace.catalog.config.AppConfig;
import com.marketplace.catalog.config.Config;
import com.marketplace.catalog.db.ConnectionFactory;
import com.marketplace.catalog.db.LiquibaseRunner;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class LiquibaseInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Config config = new AppConfig();
        ConnectionFactory connectionFactory = new ConnectionFactory(config);

        LiquibaseRunner runner = new LiquibaseRunner(config, connectionFactory);
        runner.migrate();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // тут нам ничего делать не нужно
    }
}