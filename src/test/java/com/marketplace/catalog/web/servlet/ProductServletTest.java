package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.ProductService;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.ProductDto;
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

import static com.marketplace.catalog.web.servlet.TestUtils.toServletInputStream;
import static org.junit.jupiter.api.Assertions.*;
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
        this.productService = mock(ProductService.class);
        this.productMapper = Mappers.getMapper(ProductMapper.class);
        this.objectMapper = ObjectMapperFactory.get();
        this.validator = mock(Validator.class);

        this.servlet = new ProductServlet(productService, productMapper, objectMapper, validator);
    }

    @Test
    void givenViewerUser_whenGetAllWithoutFilters_then200AndProductsReturned() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn(null);           // /api/products
        when(req.getParameter(anyString())).thenReturn(null); // без фильтров

        when(req.getSession(false)).thenReturn(session);
        User viewer = new User(2L, "user", "pwd", Role.VIEWER);
        when(session.getAttribute("currentUser")).thenReturn(viewer);

        Product p = new Product(
                1L,
                "Phone",
                "ACME",
                Category.ELECTRONICS,
                new BigDecimal("100.00"),
                "Smartphone"
        );
        when(productService.findAll()).thenReturn(List.of(p));

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doGet(req, resp);

        // then
        verify(productService).findAll();
        verify(resp).setStatus(HttpServletResponse.SC_OK);

        ProductDto[] dtos = objectMapper.readValue(sw.toString(), ProductDto[].class);
        assertEquals(1, dtos.length);
        assertEquals(1L, dtos[0].id());
        assertEquals("Phone", dtos[0].name());
        assertEquals("ACME", dtos[0].brand());
        assertEquals("ELECTRONICS", dtos[0].category());
    }

    @Test
    void givenNoUser_whenGetAll_then401AndErrorReturned() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getPathInfo()).thenReturn(null);
        when(req.getSession(false)).thenReturn(null);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doGet(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse error = objectMapper.readValue(sw.toString(), ErrorResponse.class);
        assertEquals("Not authorized", error.message());
        assertNotNull(error.details());
        assertTrue(error.details().contains("Login required"));
    }

    @Test
    void givenAdminUserAndValidRequest_whenCreateProduct_then201AndProductReturned() throws Exception {
        // given
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

        Product saved = new Product(
                10L,
                "Laptop",
                "ACME",
                Category.ELECTRONICS,
                new BigDecimal("1000.0"),
                "Gaming laptop"
        );
        when(productService.createProduct(any(Product.class), eq("admin")))
                .thenReturn(saved);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doPost(req, resp);

        // then
        verify(productService).createProduct(any(Product.class), eq("admin"));
        verify(resp).setStatus(HttpServletResponse.SC_CREATED);

        ProductDto dto = objectMapper.readValue(sw.toString(), ProductDto.class);
        assertEquals(10L, dto.id());
        assertEquals("Laptop", dto.name());
        assertEquals("ACME", dto.brand());
        assertEquals("ELECTRONICS", dto.category());
        assertEquals(new BigDecimal("1000.0"), dto.price());
    }

    @Test
    void givenNonAdminUser_whenCreateProduct_then403AndErrorReturned() throws Exception {
        // given
        HttpServletRequest req  = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getPathInfo()).thenReturn(null);
        when(req.getSession(false)).thenReturn(session);

        User user = new User(2L, "user", "pwd", Role.VIEWER);
        when(session.getAttribute("currentUser")).thenReturn(user);

        String json = """
                {
                  "name": "Laptop",
                  "brand": "ACME",
                  "category": "ELECTRONICS",
                  "price": 1000.0
                }
                """;
        when(req.getInputStream()).thenReturn(toServletInputStream(json));

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        // when
        servlet.doPost(req, resp);

        // then
        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(productService, never()).createProduct(any(), anyString());

        ErrorResponse error = objectMapper.readValue(sw.toString(), ErrorResponse.class);
        assertEquals("Admin role required", error.message());
    }
}
