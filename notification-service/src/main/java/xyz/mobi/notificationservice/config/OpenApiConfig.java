package xyz.mobi.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Email Notification Microservice API")
                        .description("Reusable REST API for sending emails via Microsoft Outlook SMTP and tracking notification delivery status.")
                        .version("1.0.0")
                        .contact(new Contact().name("Notification Service Team").email("support@example.com")));
    }
}
