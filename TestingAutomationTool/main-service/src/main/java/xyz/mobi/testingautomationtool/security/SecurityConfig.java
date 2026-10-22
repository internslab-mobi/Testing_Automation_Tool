package xyz.mobi.testingautomationtool.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration configuration = new CorsConfiguration();
//         configuration.setAllowedOriginPatterns(List.of("*"));
//         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//         configuration.setAllowedHeaders(Arrays.asList(
//                 "Authorization",
//                 "Content-Type",
//                 "X-Requested-With",
//                 "Accept",
//                 "Origin",
//                 "Access-Control-Request-Method",
//                 "Access-Control-Request-Headers",
//                 "Cookie",
//                 "X-XSRF-TOKEN",
//                 "X-CSRF-TOKEN"
//         ));
//         configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie", "XSRF-TOKEN"));
//         configuration.setAllowCredentials(true);
//         configuration.setMaxAge(3600L);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
//         return source;
//     }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF protection is managed centrally at the API Gateway level
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/static/**", "/bug-chat.html", "/manager-dashboard.html", "/admin-portal.html", "/tester-workspace.html", "/favicon.ico", "/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Manager & Admin endpoints: Dashboard & Project status toggle
                        .requestMatchers("/manager/users/active").hasAnyRole("MANAGER", "ADMIN", "TESTER")
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/project/*/status", "/project/active", "/project/inactive").hasAnyRole("MANAGER", "ADMIN")

                        // Authenticated application endpoints (MANAGER, TESTER, ADMIN, DEVELOPER)
                        .requestMatchers("/bugs/**", "/bug/**").hasAnyRole("MANAGER", "TESTER", "ADMIN", "DEVELOPER")
                        .requestMatchers("/feature/**", "/features/**").hasAnyRole("MANAGER", "TESTER", "ADMIN", "DEVELOPER")
                        .requestMatchers("/project/**", "/projects/**").hasAnyRole("MANAGER", "TESTER", "ADMIN", "DEVELOPER")
                        .requestMatchers("/testcases/**", "/testcase/**").hasAnyRole("MANAGER", "TESTER", "ADMIN", "DEVELOPER")
                        .requestMatchers("/comments/**").hasAnyRole("MANAGER", "TESTER", "ADMIN", "DEVELOPER")
                        .requestMatchers("/attachments/**").hasAnyRole("MANAGER", "TESTER", "ADMIN", "DEVELOPER")
                        .requestMatchers("/notifications/**", "/in-app-notifications/**").hasAnyRole("MANAGER", "TESTER", "ADMIN", "DEVELOPER")

                        // Any other request
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
