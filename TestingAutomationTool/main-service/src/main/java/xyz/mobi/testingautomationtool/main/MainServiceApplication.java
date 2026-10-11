package xyz.mobi.testingautomationtool.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "xyz.mobi.testingautomationtool")
@EnableJpaRepositories(basePackages = "xyz.mobi.testingautomationtool")
@EnableJpaAuditing
@EnableCaching
public class MainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainServiceApplication.class, args);
    }
}
