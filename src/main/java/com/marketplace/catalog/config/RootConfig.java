package com.marketplace.catalog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.db.ConnectionFactory;
import com.marketplace.catalog.db.LiquibaseRunner;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.repository.UserRepository;
import com.marketplace.catalog.repository.impl.jdbc.JdbcProductRepository;
import com.marketplace.catalog.repository.impl.jdbc.JdbcUserRepository;
import com.marketplace.catalog.service.AuthService;
import com.marketplace.catalog.service.Metrics;
import com.marketplace.catalog.service.ProductService;
import com.marketplace.catalog.service.impl.AuthServiceImpl;
import com.marketplace.catalog.service.impl.InMemoryMetrics;
import com.marketplace.catalog.service.impl.ProductServiceImpl;
import com.marketplace.catalog.web.json.ObjectMapperFactory;
import com.marketplace.catalog.web.mapper.ProductMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Configuration
@ComponentScan(basePackages = "com.marketplace.catalog")
@EnableAspectJAutoProxy
@PropertySource(
        value = "classpath:application.yml",
        factory = YamlPropertySourceFactory.class
)
public class RootConfig {

    @Bean
    public Config appConfig(Environment env) {
        return new AppConfig(env);
    }

    @Bean
    public ConnectionFactory connectionFactory(Config config) {
        return new ConnectionFactory(config);
    }

    @Bean
    public UserRepository userRepository(ConnectionFactory connectionFactory, Config config) {
        return new JdbcUserRepository(connectionFactory, config.getDbSchema());
    }

    @Bean
    public ProductRepository productRepository(ConnectionFactory connectionFactory, Config config) {
        return new JdbcProductRepository(connectionFactory, config.getDbSchema());
    }

    @Bean
    public Metrics metrics() {
        return new InMemoryMetrics();
    }

    @Bean
    public ProductService productService(ProductRepository productRepository, Metrics metrics) {
        return new ProductServiceImpl(productRepository, metrics);
    }

    @Bean
    public AuthService authService(UserRepository userRepository) {
        return new AuthServiceImpl(userRepository);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapperFactory.get();
    }

    @Bean
    public ProductMapper productMapper() {
        return Mappers.getMapper(ProductMapper.class);
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean(initMethod = "migrate")
    public LiquibaseRunner liquibaseRunner(Config config, ConnectionFactory connectionFactory) {
        return new LiquibaseRunner(config, connectionFactory);
    }
}