package com.marketplace.catalog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.db.ConnectionFactory;
import com.marketplace.catalog.repository.UserRepository;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.repository.impl.jdbc.JdbcUserRepository;
import com.marketplace.catalog.repository.impl.jdbc.JdbcProductRepository;
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
import lombok.Getter;
import org.mapstruct.factory.Mappers;

public class AppContext {

    @Getter
    private final Config config;
    private final ConnectionFactory connectionFactory;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Getter
    private final Metrics metrics;
    @Getter
    private final AuthService authService;
    @Getter
    private final ProductService productService;

    @Getter
    private final ObjectMapper objectMapper;
    @Getter
    private final ProductMapper productMapper;
    @Getter
    private final Validator validator;

    public AppContext() {
        this.config = new AppConfig(); // <-- вот тут твой существующий AppConfig
        this.connectionFactory = new ConnectionFactory(config);

        this.userRepository = new JdbcUserRepository(connectionFactory, config.getDbSchema());
        this.productRepository = new JdbcProductRepository(connectionFactory, config.getDbSchema());

        this.metrics = new InMemoryMetrics();
        this.productService = new ProductServiceImpl(productRepository, metrics);
        this.authService = new AuthServiceImpl(userRepository);

        this.objectMapper = ObjectMapperFactory.get();
        this.productMapper = Mappers.getMapper(ProductMapper.class);
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

}