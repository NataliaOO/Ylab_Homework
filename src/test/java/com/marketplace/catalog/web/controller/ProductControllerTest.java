package com.marketplace.catalog.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.model.Product;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.ProductService;
import com.marketplace.catalog.web.dto.ProductDto;
import com.marketplace.catalog.web.dto.ProductRequest;
import com.marketplace.catalog.web.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static com.marketplace.catalog.TestData.*;
import static com.marketplace.catalog.web.controller.AuthController.ATTR_CURRENT_USER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getProducts_shouldReturn403_whenUserAnonymous() throws Exception {
        // given

        // when / then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProducts_shouldReturnList_whenUserIsViewer() throws Exception {
        // given
        User viewer = viewerUser();

        Product product = productPenV2(100L);
        ProductDto dto = productDtoPenV2_249(100L);

        when(productService.findAll()).thenReturn(List.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        // when / then
        mockMvc.perform(get("/api/products")
                        .sessionAttr(ATTR_CURRENT_USER, viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].name").value(NAME_PEN_V2))
                .andExpect(jsonPath("$[0].price").value(PRICE_249.doubleValue()));
    }

    @Test
    void deleteProduct_shouldReturn204_whenUserIsAdminAndProductExists() throws Exception {
        // given
        User admin = adminUser();
        long id = 100L;

        when(productService.deleteProduct(eq(id), anyString())).thenReturn(true);

        // when / then
        mockMvc.perform(delete("/api/products/{id}", id)
                        .sessionAttr(ATTR_CURRENT_USER, admin))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateProduct_shouldReturn200_whenUserIsAdminAndProductExists() throws Exception {
        // given
        User admin = adminUser();
        long id = 100L;

        ProductRequest request = productRequestPenV2_249();
        Product updatedEntity = productPenV2(id);
        ProductDto dto = productDtoPenV2_249(id);

        when(productMapper.fromRequest(any(ProductRequest.class))).thenReturn(updatedEntity);
        when(productService.updateProduct(eq(id), any(Product.class), anyString()))
                .thenReturn(Optional.of(updatedEntity));
        when(productMapper.toDto(updatedEntity)).thenReturn(dto);

        // when / then
        mockMvc.perform(put("/api/products/{id}", id)
                        .sessionAttr(ATTR_CURRENT_USER, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.name").value(NAME_PEN_V2))
                .andExpect(jsonPath("$.price").value(PRICE_249.doubleValue()));
    }

    @Test
    void deleteProduct_shouldReturn404_whenUserIsAdminAndProductNotFound() throws Exception {
        // given
        User admin = adminUser();
        long id = 999L;

        when(productService.deleteProduct(eq(id), anyString())).thenReturn(false);

        // when / then
        mockMvc.perform(delete("/api/products/{id}", id)
                        .sessionAttr(ATTR_CURRENT_USER, admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: id=" + id));
    }
}
