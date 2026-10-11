package xyz.mobi.testingautomationtool.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "xyz.mobi.testingautomationtool")
@EnableJpaRepositories(basePackages = "xyz.mobi.testingautomationtool")
@EnableJpaAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
