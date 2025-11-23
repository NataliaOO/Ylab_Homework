package com.marketplace.catalog.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.config.AppContext;
import com.marketplace.catalog.exception.ProductValidationException;
import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.ProductService;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.ProductDto;
import com.marketplace.catalog.web.dto.ProductRequest;
import com.marketplace.catalog.web.mapper.ProductMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(name = "ProductServlet", urlPatterns = "/api/products/*")
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private ProductMapper productMapper;
    private ObjectMapper objectMapper;
    private Validator validator;

    public ProductServlet(ProductService productService,
                          ProductMapper productMapper,
                          ObjectMapper objectMapper,
                          Validator validator) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public ProductServlet() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        AppContext ctx = (AppContext) config.getServletContext().getAttribute("appContext");

        this.productService = ctx.getProductService();
        this.productMapper  = ctx.getProductMapper();
        this.objectMapper   = ctx.getObjectMapper();
        this.validator      = ctx.getValidator();
    }

    // ---------- READ ----------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);

        User user = getCurrentUser(req);
        if (!hasViewAccess(user)) {
            sendUnauthorized(resp);
            return;
        }

        try {
            if (isRootPath(req.getPathInfo())) {
                handleSearchOrList(req, resp);
            } else {
                sendNotFound(resp,
                        "GET by id is not supported",
                        List.of("Use /api/products with filters"));
            }
        } catch (Exception e) {
            sendInternalError(resp, e);
        }
    }

    // ---------- CREATE ----------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);

        User user = requireAdmin(req, resp);
        if (user == null) {
            return;
        }

        try {
            ProductRequest productRequest = readAndValidateRequest(req, resp);
            if (productRequest == null) {
                return;
            }

            Product product = productMapper.fromRequest(productRequest);
            Product saved = productService.createProduct(product, user.getLogin());

            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), productMapper.toDto(saved));
        } catch (ProductValidationException e) {
            sendBadRequest(resp, e.getMessage(), List.of());
        } catch (Exception e) {
            sendInternalError(resp, e);
        }
    }

    // ---------- UPDATE ----------

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);

        User user = requireAdmin(req, resp);
        if (user == null) {
            return;
        }

        Long id = parseAndValidateId(req.getPathInfo(), resp);
        if (id == null) {
            return;
        }

        try {
            ProductRequest productRequest = readAndValidateRequest(req, resp);
            if (productRequest == null) {
                return;
            }

            Product productToUpdate = productMapper.fromRequest(productRequest);
            var updatedOpt = productService.updateProduct(id, productToUpdate, user.getLogin());

            if (updatedOpt.isEmpty()) {
                sendNotFound(resp, "Product not found", List.of("id=" + id));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), productMapper.toDto(updatedOpt.get()));
        } catch (ProductValidationException e) {
            sendBadRequest(resp, e.getMessage(), List.of());
        } catch (Exception e) {
            sendInternalError(resp, e);
        }
    }

    // ---------- DELETE ----------

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareJsonResponse(resp);

        User user = requireAdmin(req, resp);
        if (user == null) {
            return;
        }

        Long id = parseAndValidateId(req.getPathInfo(), resp);
        if (id == null) {
            return;
        }

        boolean deleted = productService.deleteProduct(id, user.getLogin());

        if (!deleted) {
            sendNotFound(resp, "Product not found", List.of("id=" + id));
            return;
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void handleSearchOrList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Category category = parseCategoryOrRespond(req.getParameter("category"), resp);
        if (category == null && hasParam(req, "category")) {
            return; // ошибка уже отправлена
        }

        BigDecimal minPrice = parsePriceOrRespond(req.getParameter("minPrice"), resp);
        if (minPrice == null && hasParam(req, "minPrice")
                && !isNumericOrNull(req.getParameter("minPrice"))) {
            return;
        }

        BigDecimal maxPrice = parsePriceOrRespond(req.getParameter("maxPrice"), resp);
        if (maxPrice == null && hasParam(req, "maxPrice")
                && !isNumericOrNull(req.getParameter("maxPrice"))) {
            return;
        }

        String brand = req.getParameter("brand");
        String text  = req.getParameter("text");

        List<ProductDto> result = shouldUseSimpleList(category, brand, minPrice, maxPrice, text)
                ? listAllProducts()
                : searchProducts(category, brand, minPrice, maxPrice, text);

        resp.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(resp.getWriter(), result);
    }

    private boolean shouldUseSimpleList(Category category,
                                        String brand,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        String text) {
        return category == null
                && brand == null
                && minPrice == null
                && maxPrice == null
                && text == null;
    }

    private List<ProductDto> listAllProducts() {
        return productService.findAll().stream()
                .map(productMapper::toDto)
                .toList();
    }

    private List<ProductDto> searchProducts(Category category,
                                            String brand,
                                            BigDecimal minPrice,
                                            BigDecimal maxPrice,
                                            String text) {
        return productService.search(category, brand, minPrice, maxPrice, text).stream()
                .map(productMapper::toDto)
                .toList();
    }

    private User requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (!isAdmin(user)) {
            sendForbidden(resp, "Admin role required");
            return null;
        }
        return user;
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    private boolean hasViewAccess(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole() == Role.ADMIN || user.getRole() == Role.VIEWER;
    }

    private boolean isRootPath(String pathInfo) {
        return pathInfo == null || "/".equals(pathInfo);
    }

    private Long parseAndValidateId(String pathInfo, HttpServletResponse resp) throws IOException {
        Long id = parseId(pathInfo);
        if (id == null) {
            sendBadRequest(resp, "Invalid id in path", List.of(pathInfo));
        }
        return id;
    }

    private Long parseId(String pathInfo) {
        if (pathInfo == null || "/".equals(pathInfo)) {
            return null;
        }
        String raw = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ProductRequest readAndValidateRequest(HttpServletRequest req,
                                                  HttpServletResponse resp) throws IOException {
        ProductRequest productRequest =
                objectMapper.readValue(req.getInputStream(), ProductRequest.class);

        List<String> errors = validate(productRequest);
        if (!errors.isEmpty()) {
            sendBadRequest(resp, "Validation failed", errors);
            return null;
        }
        return productRequest;
    }

    private Category parseCategoryOrRespond(String raw, HttpServletResponse resp) throws IOException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Category.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            sendBadRequest(resp, "Invalid category value", List.of(raw));
            return null;
        }
    }

    private BigDecimal parsePriceOrRespond(String raw, HttpServletResponse resp) throws IOException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            sendBadRequest(resp, "Invalid price value", List.of(raw));
            return null;
        }
    }

    private boolean hasParam(HttpServletRequest req, String name) {
        return req.getParameter(name) != null;
    }

    private boolean isNumericOrNull(String raw) {
        if (raw == null || raw.isBlank()) return true;
        try {
            new BigDecimal(raw);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private <T> List<String> validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        return violations.stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.toList());
    }

    private void sendBadRequest(HttpServletResponse resp, String message, List<String> details) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        objectMapper.writeValue(resp.getWriter(), new ErrorResponse(message, details));
    }

    private void sendNotFound(HttpServletResponse resp, String message, List<String> details) throws IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        objectMapper.writeValue(resp.getWriter(), new ErrorResponse(message, details));
    }

    private void sendForbidden(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        objectMapper.writeValue(resp.getWriter(), new ErrorResponse(message, null));
    }

    private void sendUnauthorized(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(resp.getWriter(),
                new ErrorResponse("Not authorized", List.of("Login required")));
    }

    private void sendInternalError(HttpServletResponse resp, Exception e) throws IOException {
        e.printStackTrace();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        objectMapper.writeValue(resp.getWriter(),
                new ErrorResponse("Internal server error", List.of(e.getMessage())));
    }

    private void prepareJsonResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
