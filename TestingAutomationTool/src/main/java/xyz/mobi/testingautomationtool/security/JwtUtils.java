package xyz.mobi.testingautomationtool.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtils {

    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:900000}")
    private long jwtExpirationMs = 900000L;

    // 15 Minutes = 900,000 ms
    @Getter
    @Value("${app.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs = 900000L;

    // 24 Hours = 86,400,000 ms
    @Getter
    @Value("${app.jwt.refresh-token-expiration-ms:86400000}")
    private long refreshTokenExpirationMs = 86400000L;

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                keyBytes = md.digest(keyBytes);
            } catch (Exception ignored) {}
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private long getEffectiveAccessTokenExpirationMs() {
        return accessTokenExpirationMs > 0 ? accessTokenExpirationMs : (jwtExpirationMs > 0 ? jwtExpirationMs : 900000L);
    }

    private long getEffectiveRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs > 0 ? refreshTokenExpirationMs : 86400000L;
    }

    /**
     * Backward-compatible token generation (generates 15-minute access token)
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return generateAccessToken(userPrincipal);
    }

    public String generateTokenFromUserDetails(CustomUserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    public String generateTokenFromUsername(String username, Integer userId, String email, String role, String fullName) {
        return generateAccessTokenFromUsername(username, userId, email, role, fullName);
    }

    /**
     * Generates a 15-minute Access Token containing user claims and role
     */
    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getEffectiveAccessTokenExpirationMs());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
                .claim("userId", userDetails.getUserId())
                .claim("email", userDetails.getEmail())
                .claim("role", userDetails.getRoleName())
                .claim("fullName", userDetails.getFullName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generates a 24-hour Refresh Token
     */
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getEffectiveRefreshTokenExpirationMs());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
                .claim("userId", userDetails.getUserId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateAccessTokenFromUsername(String username, Integer userId, String email, String role, String fullName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getEffectiveAccessTokenExpirationMs());

        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role)
                .claim("fullName", fullName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshTokenFromUsername(String username, Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getEffectiveRefreshTokenExpirationMs());

        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Integer.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public String getTokenType(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get(TOKEN_TYPE_CLAIM, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Calculates remaining time before expiration in milliseconds
     */
    public long getRemainingExpirationMs(String token) {
        try {
            Date expiration = getClaimFromToken(token, Claims::getExpiration);
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Validates access token (must be valid signature, not expired, and of type ACCESS)
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return tokenType == null || TOKEN_TYPE_ACCESS.equalsIgnoreCase(tokenType);
        } catch (Exception e) {
            log.error("Access token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates refresh token (must be valid signature, not expired, and of type REFRESH)
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return TOKEN_TYPE_REFRESH.equalsIgnoreCase(tokenType);
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token is expired: {}", e.getMessage());
            throw e; // re-throw so callers can specifically catch expired session
        } catch (Exception e) {
            log.error("Refresh token validation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

}
