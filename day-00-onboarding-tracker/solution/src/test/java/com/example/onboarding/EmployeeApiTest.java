package com.example.onboarding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

// The brief's contract, tested through the whole app with a fresh in-memory database
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:employees")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // a fresh, empty database for every test
class EmployeeApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void listIsEmptyBeforeAnythingIsSaved() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void aValidEmployeeIsCapturedWithAGeneratedId() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Asha Rao", "role": "Engineer", "onboardingDate": "2026-11-10" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Asha Rao"))
                .andExpect(jsonPath("$.role").value("Engineer"))
                .andExpect(jsonPath("$.onboardingDate").value("2026-11-10"));
    }

    @Test
    void capturedEmployeesAreListed() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Asha Rao", "role": "Engineer", "onboardingDate": "2026-11-10" }
                        """));
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Ben Ortiz", "role": "Analyst", "onboardingDate": "2026-11-17" }
                        """));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Ben Ortiz"));
    }

    @Test
    void anEmptyBodyIsRejected() throws Exception {
        create("{}").andExpect(status().isBadRequest());
    }

    @Test
    void aMissingRoleIsRejected() throws Exception {
        create("""
                { "name": "Asha Rao", "onboardingDate": "2026-11-10" }
                """).andExpect(status().isBadRequest());
    }

    @Test
    void aBlankNameIsRejected() throws Exception {
        create("""
                { "name": "  ", "role": "Engineer", "onboardingDate": "2026-11-10" }
                """).andExpect(status().isBadRequest());
    }

    @Test
    void aDateInTheWrongFormatIsRejected() throws Exception {
        create("""
                { "name": "Asha Rao", "role": "Engineer", "onboardingDate": "10-11-2026" }
                """).andExpect(status().isBadRequest());
    }

    @Test
    void aDateThatDoesNotExistIsRejected() throws Exception {
        create("""
                { "name": "Asha Rao", "role": "Engineer", "onboardingDate": "2026-02-30" }
                """).andExpect(status().isBadRequest());
    }

    private ResultActions create(String json) throws Exception {
        return mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
