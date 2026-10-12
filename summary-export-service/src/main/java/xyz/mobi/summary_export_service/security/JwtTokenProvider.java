package xyz.mobi.summary_export_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
            if (keyBytes.length < 32) {
                keyBytes = java.util.HexFormat.of().parseHex(jwtSecret);
            }
        } catch (Exception e) {
            try {
                keyBytes = java.util.HexFormat.of().parseHex(jwtSecret);
            } catch (Exception e2) {
                keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String sub = claims.getSubject();
        if (sub != null && !sub.isBlank()) {
            return sub;
        }
        Object username = claims.get("username");
        if (username != null) {
            return username.toString();
        }
        return "authenticated_user";
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Check "role" claim
        Object roleObj = claims.get("role");
        if (roleObj instanceof String roleStr) {
            addAuthority(authorities, roleStr);
        } else if (roleObj instanceof Map<?, ?> roleMap) {
            Object r = roleMap.get("role");
            if (r != null) {
                addAuthority(authorities, r.toString());
            }
        } else if (roleObj instanceof Collection<?> roleCol) {
            for (Object r : roleCol) {
                if (r != null) addAuthority(authorities, r.toString());
            }
        }

        // Check "roles" claim
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof Collection<?> roleCol) {
            for (Object r : roleCol) {
                if (r != null) addAuthority(authorities, r.toString());
            }
        } else if (rolesObj instanceof String rolesStr) {
            for (String r : rolesStr.split(",")) {
                addAuthority(authorities, r.trim());
            }
        }

        // Check "authorities" claim
        Object authObj = claims.get("authorities");
        if (authObj instanceof Collection<?> authCol) {
            for (Object a : authCol) {
                if (a instanceof Map<?, ?> authMap) {
                    Object authority = authMap.get("authority");
                    if (authority != null) addAuthority(authorities, authority.toString());
                } else if (a != null) {
                    addAuthority(authorities, a.toString());
                }
            }
        }

        return authorities;
    }

    private void addAuthority(List<GrantedAuthority> authorities, String roleName) {
        if (roleName == null || roleName.isBlank()) return;
        String cleanRole = roleName.trim();
        if (!cleanRole.startsWith("ROLE_")) {
            cleanRole = "ROLE_" + cleanRole;
        }
        authorities.add(new SimpleGrantedAuthority(cleanRole));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
