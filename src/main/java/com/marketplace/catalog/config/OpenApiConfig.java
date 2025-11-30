package com.marketplace.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Конфигурация OpenAPI/Swagger для non-Spring-Boot приложения.
 */
@Configuration
@Import({
        SwaggerConfig.class,
        SpringDocConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SpringDocConfigProperties.class,
        SwaggerUiConfigParameters.class,
        SwaggerUiConfigProperties.class,
        SwaggerUiOAuthProperties.class
})
@ComponentScan(basePackages = "org.springdoc")
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Catalog API")
                        .version("1.0.0")
                        .description("REST API for product catalog"));
    }
}
