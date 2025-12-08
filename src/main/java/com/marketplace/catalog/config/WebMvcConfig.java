package com.marketplace.catalog.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web-контекст приложения.
 * <ul>
 *     <li>включает Spring MVC (@EnableWebMvc),</li>
 *     <li>сканирует REST-контроллеры в пакете {@code com.marketplace.catalog.web.controller},</li>
 *     <li>подключает Swagger / OpenAPI через {@link OpenApiConfig}.</li>
 * </ul>
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.marketplace.catalog.web.controller")
@Import(OpenApiConfig.class)
public class WebMvcConfig implements WebMvcConfigurer {

}
