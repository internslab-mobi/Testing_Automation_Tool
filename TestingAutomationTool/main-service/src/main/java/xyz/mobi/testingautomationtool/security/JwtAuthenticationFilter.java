package xyz.mobi.testingautomationtool.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                if (jwtUtils.validateAccessToken(jwt)) {
                    String username = jwtUtils.getUsernameFromToken(jwt);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (userDetails != null && userDetails.isEnabled()) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    String tokenType = jwtUtils.getTokenType(jwt);
                    if (JwtUtils.TOKEN_TYPE_REFRESH.equalsIgnoreCase(tokenType)) {
                        request.setAttribute("jwt_error_message", "Refresh token cannot be used to authenticate API requests. Please call /auth/refresh to obtain an access token.");
                    }
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT access token is expired: {}", e.getMessage());
            request.setAttribute("jwt_expired", true);
            request.setAttribute("jwt_error_message", "Access token has expired (validity: 15 minutes). Please use your refresh token at /auth/refresh to get a new access token.");
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            request.setAttribute("jwt_error_message", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
