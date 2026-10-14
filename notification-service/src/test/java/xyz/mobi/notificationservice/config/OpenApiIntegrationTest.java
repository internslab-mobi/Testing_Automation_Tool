package xyz.mobi.notificationservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class OpenApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should expose /v3/api-docs and include active endpoints")
    void testOpenApiDocumentationEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()))
                .andExpect(jsonPath("$.info.title").value("Email Notification Microservice API"))
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.paths['/api/v1/notifications/email'].post", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/notifications/{notificationId}'].get", notNullValue()))
                .andExpect(jsonPath("$.paths['/api/v1/notifications'].get", notNullValue()));
    }

    @Test
    @DisplayName("Should allow access to Swagger UI redirect endpoint")
    void testSwaggerUiEndpointPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
