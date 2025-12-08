package com.marketplace.catalog.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.AuthService;
import com.marketplace.catalog.web.dto.AuthRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static com.marketplace.catalog.TestData.adminUser;
import static com.marketplace.catalog.TestData.viewerUser;
import static com.marketplace.catalog.web.controller.AuthController.ATTR_CURRENT_USER;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_shouldReturn200AndUser_whenCredentialsValid() throws Exception {
        // given
        User user = adminUser();
        when(authService.login(anyString(), anyString())).thenReturn(Optional.of(user));

        AuthRequest request = new AuthRequest("admin", "secret");

        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        // given
        when(authService.login(anyString(), anyString())).thenReturn(Optional.empty());
        AuthRequest request = new AuthRequest("admin", "wrong");

        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void me_shouldReturn401_whenUserNotInSession() throws Exception {
        // given – пустая сессия

        // when / then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authorized"));
    }

    @Test
    void me_shouldReturnUserDto_whenUserInSession() throws Exception {
        // given
        User user = viewerUser();

        // when / then
        mockMvc.perform(get("/api/auth/me")
                        .sessionAttr(ATTR_CURRENT_USER, user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("viewer"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }
}
