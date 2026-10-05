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
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpirationMs", 900000L);   // 15 minutes
        ReflectionTestUtils.setField(jwtUtils, "refreshTokenExpirationMs", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 900000L);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
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

        String accessToken = jwtUtils.generateAccessToken(userDetails);
        assertNotNull(accessToken);
        assertTrue(jwtUtils.validateJwtToken(accessToken));
        assertTrue(jwtUtils.validateAccessToken(accessToken));
        assertEquals("ACCESS", jwtUtils.getTokenType(accessToken));
        assertEquals("testmanager", jwtUtils.getUsernameFromToken(accessToken));
        assertEquals(10, jwtUtils.getUserIdFromToken(accessToken));
        assertEquals("MANAGER", jwtUtils.getRoleFromToken(accessToken));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        Role role = Role.builder().roleId(2).role("DEVELOPER").build();
        User user = User.builder()
                .userId(15)
                .username("devuser")
                .email("dev@example.com")
                .fullName("Dev User")
                .role(role)
                .passwordHash("hashedpass")
                .isActive(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String refreshToken = jwtUtils.generateRefreshToken(userDetails);
        assertNotNull(refreshToken);
        assertTrue(jwtUtils.validateJwtToken(refreshToken));
        assertTrue(jwtUtils.validateRefreshToken(refreshToken));
        assertEquals("REFRESH", jwtUtils.getTokenType(refreshToken));
        assertEquals("devuser", jwtUtils.getUsernameFromToken(refreshToken));
        assertTrue(jwtUtils.getRemainingExpirationMs(refreshToken) > 80000000L);
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
