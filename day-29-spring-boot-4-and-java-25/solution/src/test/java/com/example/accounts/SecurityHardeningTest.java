package com.example.accounts;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

// Regression tests for the audit findings. They fail until the findings are fixed.
@SpringBootTest
@AutoConfigureMockMvc
class SecurityHardeningTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCallersCannotReachOtherActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/heapdump"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void sensitiveEndpointsAreNotExposedAtAll() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/heapdump"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/actuator/shutdown"))
                .andExpect(status().isNotFound());
    }
}
