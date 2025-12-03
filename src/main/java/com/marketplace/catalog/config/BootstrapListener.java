package com.marketplace.catalog.config;

import com.marketplace.catalog.service.Metrics;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class BootstrapListener implements ServletContextListener {

    public static final String ATTR_APP_CONTEXT = "appContext";
    public static final String ATTR_METRICS     = "metrics";

    private static final Logger log = Logger.getLogger(BootstrapListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        try {
            AppContext appContext = new AppContext();
            Metrics metrics = appContext.getMetrics();

            sc.setAttribute(ATTR_APP_CONTEXT, appContext);
            sc.setAttribute(ATTR_METRICS, metrics);

            log.info("AppContext and Metrics successfully initialized");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize application context", e);
            throw new IllegalStateException("Failed to initialize AppContext", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("ServletContext is being destroyed");
    }
}