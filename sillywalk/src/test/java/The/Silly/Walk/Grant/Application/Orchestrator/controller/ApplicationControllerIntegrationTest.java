package The.Silly.Walk.Grant.Application.Orchestrator.controller;

import The.Silly.Walk.Grant.Application.Orchestrator.dto.ApplicationSubmissionRequest;
import The.Silly.Walk.Grant.Application.Orchestrator.service.GrantApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Ministry of Silly Walks Application Controller.
 * Tests security, validation, and API compliance.
 */
@SpringBootTest
@AutoConfigureWebMvc
class ApplicationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrantApplicationService applicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(header().exists("X-Ministry-Status"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testApplicationSubmissionValidation() throws Exception {
        // Test with invalid data (too short description)
        ApplicationSubmissionRequest invalidRequest = new ApplicationSubmissionRequest(
                "John Cleese",
                "Test Walk",
                "Short", // Too short description
                true,
                false,
                3
        );

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        ApplicationSubmissionRequest request = new ApplicationSubmissionRequest(
                "John Cleese",
                "The Ministry March",
                "A wonderfully silly walk involving briefcase swinging and synchronized stepping that demonstrates the highest levels of absurdity and comedic timing required by the Ministry standards.",
                true,
                true,
                5
        );

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testStatisticsEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/statistics")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"));
    }
}