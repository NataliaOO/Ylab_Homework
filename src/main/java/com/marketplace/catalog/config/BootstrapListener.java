package com.marketplace.catalog.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class BootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AppContext appContext = new AppContext();
        ServletContext sc = sce.getServletContext();

        sc.setAttribute("appContext", appContext);
        sc.setAttribute("metrics", appContext.getMetrics());
    }
}