package com.marketplace.catalog.web.controller;

import com.marketplace.catalog.model.User;
import com.marketplace.catalog.service.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.marketplace.catalog.TestData.adminUser;
import static com.marketplace.catalog.TestData.viewerUser;
import static com.marketplace.catalog.web.controller.AuthController.ATTR_CURRENT_USER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    @Mock
    private Metrics metrics;

    @InjectMocks
    private MetricsController metricsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(metricsController).build();
    }

    @Test
    void getMetrics_shouldReturn401_whenUserAnonymous() throws Exception {
        // given

        // when / then
        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMetrics_shouldReturn403_whenUserIsViewer() throws Exception {
        // given
        User viewer = viewerUser();

        // when / then
        mockMvc.perform(get("/api/metrics")
                        .sessionAttr(ATTR_CURRENT_USER, viewer))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMetrics_shouldReturnMetrics_whenUserIsAdmin() throws Exception {
        // given
        User admin = adminUser();

        when(metrics.getCreateCount()).thenReturn(5L);
        when(metrics.getUpdateCount()).thenReturn(2L);
        when(metrics.getDeleteCount()).thenReturn(1L);
        when(metrics.getSearchCount()).thenReturn(10L);
        when(metrics.getCacheHitCount()).thenReturn(7L);
        when(metrics.getAverageSearchTimeMillis()).thenReturn(15.5);
        when(metrics.getCacheHitRatio()).thenReturn(0.7);

        // when / then
        mockMvc.perform(get("/api/metrics")
                        .sessionAttr(ATTR_CURRENT_USER, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createCount").value(5))
                .andExpect(jsonPath("$.updateCount").value(2))
                .andExpect(jsonPath("$.deleteCount").value(1))
                .andExpect(jsonPath("$.searchCount").value(10));
    }
}
