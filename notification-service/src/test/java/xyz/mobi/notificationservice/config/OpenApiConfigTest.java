package xyz.mobi.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    @DisplayName("Should configure OpenAPI metadata and contact info")
    void testNotificationServiceOpenAPIConfiguration() {
        OpenAPI openAPI = openApiConfig.notificationServiceOpenAPI();

        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Email Notification Microservice API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getInfo().getDescription());
        assertNotNull(openAPI.getInfo().getContact());
        assertEquals("Notification Service Team", openAPI.getInfo().getContact().getName());
    }
}
