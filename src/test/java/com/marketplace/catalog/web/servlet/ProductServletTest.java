package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.ProductService;
import com.marketplace.catalog.web.json.ObjectMapperFactory;
import com.marketplace.catalog.web.mapper.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

import static com.marketplace.catalog.TestUtils.toServletInputStream;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServletTest {

    private ProductService productService;
    private ProductServlet servlet;
    private ProductMapper productMapper;
    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        productMapper = Mappers.getMapper(ProductMapper.class);
        objectMapper = ObjectMapperFactory.get();
        validator = mock(Validator.class);

        servlet = new ProductServlet(productService, productMapper, objectMapper, validator);
    }

    @Test
    void getAll_shouldReturn200AndCallService() throws Exception {
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn(null); // /api/products
        when(req.getParameter(anyString())).thenReturn(null);

        Product p = new Product(1L, "Phone", "ACME",
                Category.ELECTRONICS, new BigDecimal("100.00"), " ");

        when(productService.findAll()).thenReturn(List.of(p));

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        verify(productService).findAll();
        verify(resp).setStatus(HttpServletResponse.SC_OK);

        String body = sw.toString();
        assertTrue(body.contains("Phone"));
    }

    @Test
    void createProduct_asAdmin_shouldReturn201() throws Exception {
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn(null); // POST /api/products
        when(req.getSession(false)).thenReturn(session);

        User admin = new User(1L, "admin", "pwd", Role.ADMIN);
        when(session.getAttribute("currentUser")).thenReturn(admin);

        String json = """
                {
                  "name": "Laptop",
                  "brand": "ACME",
                  "category": "ELECTRONICS",
                  "price": 1000.0,
                  "description": "Gaming laptop"
                }
                """;
        when(req.getInputStream()).thenReturn(toServletInputStream(json));

        Product saved = new Product(10L, "Laptop", "ACME", Category.ELECTRONICS,
                new BigDecimal("1000.0"),"Gaming laptop");

        when(productService.createProduct(any(Product.class), eq("admin")))
                .thenReturn(saved);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(req, resp);

        verify(productService).createProduct(any(Product.class), eq("admin"));
        verify(resp).setStatus(HttpServletResponse.SC_CREATED);

        String body = sw.toString();
        assertTrue(body.contains("\"id\":10"));
        assertTrue(body.contains("Laptop"));
    }

    @Test
    void createProduct_nonAdmin_shouldReturn403() throws Exception {
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn(null);
        when(req.getSession(false)).thenReturn(session);

        User user = new User(2L, "user", "pwd", Role.VIEWER);
        when(session.getAttribute("currentUser")).thenReturn(user);

        String json = """
                {"name":"Laptop","brand":"ACME","category":"ELECTRONICS","price":1000.0}
                """;
        when(req.getInputStream()).thenReturn(toServletInputStream(json));

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doPost(req, resp);

        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(productService, never()).createProduct(any(), anyString());

        String body = sw.toString();
        assertTrue(body.contains("Admin role required"));
    }
}
