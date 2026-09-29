package xyz.mobi.testingautomationtool.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import xyz.mobi.testingautomationtool.entity.Role;
import xyz.mobi.testingautomationtool.entity.User;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void testGenerateAndValidateToken() {
        Role role = Role.builder().roleId(1).role("MANAGER").build();
        User user = User.builder()
                .userId(10)
                .username("testmanager")
                .email("manager@example.com")
                .fullName("Test Manager")
                .role(role)
                .passwordHash("hashedpass")
                .isActive(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtUtils.generateTokenFromUserDetails(userDetails);
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("testmanager", jwtUtils.getUsernameFromToken(token));
        assertEquals(10, jwtUtils.getUserIdFromToken(token));
        assertEquals("MANAGER", jwtUtils.getRoleFromToken(token));
    }

    @Test
    void testTesterRoleToken() {
        String token = jwtUtils.generateTokenFromUsername("testtester", 20, "tester@example.com", "TESTER", "Test Tester");
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("testtester", jwtUtils.getUsernameFromToken(token));
        assertEquals(20, jwtUtils.getUserIdFromToken(token));
        assertEquals("TESTER", jwtUtils.getRoleFromToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.here"));
    }
}
