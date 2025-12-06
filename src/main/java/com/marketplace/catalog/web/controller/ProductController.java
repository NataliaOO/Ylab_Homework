package com.marketplace.catalog.web.controller;

import com.marketplace.catalog.model.Category;
import com.marketplace.catalog.model.Role;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.ProductService;
import com.marketplace.catalog.web.dto.ErrorResponse;
import com.marketplace.catalog.web.dto.ProductDto;
import com.marketplace.catalog.web.dto.ProductRequest;
import com.marketplace.catalog.web.mapper.ProductMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.marketplace.catalog.web.controller.AuthController.ATTR_CURRENT_USER;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService,
                             ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public ResponseEntity<?> listOrSearch(
            HttpServletRequest request,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String text
    ) {
        User user = getCurrentUser(request);
        if (!hasViewAccess(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Access denied"));
        }

        List<ProductDto> result;
        if (category == null && brand == null && minPrice == null && maxPrice == null && text == null) {
            result = productService.findAll().stream()
                    .map(productMapper::toDto)
                    .toList();
        } else {
            result = productService.search(category, brand, minPrice, maxPrice, text).stream()
                    .map(productMapper::toDto)
                    .toList();
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest requestBody,
                                           HttpServletRequest request) {
        User user = requireAdminOr403(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Admin role required"));
        }

        var product = productMapper.fromRequest(requestBody);
        var created = productService.createProduct(product, user.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productMapper.toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductRequest requestBody,
                                           HttpServletRequest request) {
        User user = requireAdminOr403(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Admin role required"));
        }

        var maybeUpdated = productService.updateProduct(id, productMapper.fromRequest(requestBody), user.getLogin());

        if (maybeUpdated.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Product not found: id=" + id));
        }

        var dto = productMapper.toDto(maybeUpdated.get());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id,
                                           HttpServletRequest request) {
        User user = requireAdminOr403(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Admin role required"));
        }

        boolean deleted = productService.deleteProduct(id, user.getLogin());
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Product not found: id=" + id));
        }

        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null
                ? (User) session.getAttribute(ATTR_CURRENT_USER)
                : null;
    }

    private boolean hasViewAccess(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole() == Role.ADMIN || user.getRole() == Role.VIEWER;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    private User requireAdminOr403(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (!isAdmin(user)) {
            return null;
        }
        return user;
    }
}
