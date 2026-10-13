package xyz.mobi.testingautomationtool.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "xyz.mobi.testingautomationtool")
@EntityScan(basePackages = "xyz.mobi.testingautomationtool.entity")
@EnableJpaRepositories(basePackages = "xyz.mobi.testingautomationtool.repository")
@EnableJpaAuditing
@EnableCaching
public class MainServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainServiceApplication.class, args);
    }
}
