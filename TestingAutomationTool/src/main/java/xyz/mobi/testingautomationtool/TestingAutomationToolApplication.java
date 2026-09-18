package xyz.mobi.testingautomationtool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TestingAutomationToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestingAutomationToolApplication.class, args);
    }

}
