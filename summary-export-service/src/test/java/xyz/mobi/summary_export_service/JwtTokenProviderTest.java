package xyz.mobi.summary_export_service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import xyz.mobi.summary_export_service.security.JwtTokenProvider;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secretKey);
    }

    private String generateTestToken(String username, String role, long expirationDeltaMs) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationDeltaMs))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Should successfully validate token and extract username and ROLE_MANAGER authority")
    void testValidateAndExtractRoleManager() {
        String token = generateTestToken("manager1", "ROLE_MANAGER", 60000);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("manager1");

        List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);
        assertThat(authorities).extracting(GrantedAuthority::getAuthority).contains("ROLE_MANAGER");
    }

    @Test
    @DisplayName("Should successfully normalize role without prefix to ROLE_ADMIN")
    void testNormalizeRoleAdmin() {
        String token = generateTestToken("admin1", "ADMIN", 60000);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);
        assertThat(authorities).extracting(GrantedAuthority::getAuthority).contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should extract ROLE_TESTER authority")
    void testExtractRoleTester() {
        String token = generateTestToken("tester1", "ROLE_TESTER", 60000);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);
        assertThat(authorities).extracting(GrantedAuthority::getAuthority).contains("ROLE_TESTER");
    }

    @Test
    @DisplayName("Should reject invalid or expired tokens")
    void testInvalidToken() {
        String expiredToken = generateTestToken("tester1", "ROLE_TESTER", -10000);
        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();

        assertThat(jwtTokenProvider.validateToken("invalid.token.structure")).isFalse();
    }
}
