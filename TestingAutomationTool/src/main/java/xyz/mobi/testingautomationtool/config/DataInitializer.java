package xyz.mobi.testingautomationtool.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.entity.Role;
import xyz.mobi.testingautomationtool.repository.RoleRepository;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        List<String> defaultRoles = Arrays.asList("ADMIN", "MANAGER", "DEVELOPER", "TESTER");

        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByRole(roleName)) {
                Role role = Role.builder()
                        .role(roleName)
                        .build();
                roleRepository.save(role);
                log.info("Initialized default role: {}", roleName);
            }
        }
    }
}
