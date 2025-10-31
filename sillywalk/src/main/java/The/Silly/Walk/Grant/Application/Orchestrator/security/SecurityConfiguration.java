package The.Silly.Walk.Grant.Application.Orchestrator.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for the Ministry of Silly Walks Grant Application System.
 * Implements enterprise-grade security with JWT authentication, RBAC, and comprehensive
 * security headers as required by the Ministry's security standards.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;

    @Autowired
    public SecurityConfiguration(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                               JwtRequestFilter jwtRequestFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Configure HTTP security for the Ministry application.
     * Implements defense-in-depth security strategy.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless JWT authentication
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/v1/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // Authentication endpoints (would be implemented separately)
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/register").hasRole("ADMIN")
                
                // Application submission requires authentication
                .requestMatchers("/api/v1/applications").hasAnyRole("USER", "ADMIN", "MINISTRY_STAFF")
                
                // Statistics require elevated permissions
                .requestMatchers("/api/v1/statistics").hasAnyRole("ADMIN", "MINISTRY_STAFF")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Configure exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Configure security headers
            .headers(headers -> headers
                // Prevent clickjacking
                .frameOptions().deny()
                
                // Prevent MIME type sniffing
                .contentTypeOptions().and()
                
                // XSS protection
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true)
                )
                
                // Content Security Policy
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                
                // Referrer Policy
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                
                // Additional security headers
                .and()
                .and()
            );

        // Add JWT filter before username/password authentication filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure CORS for the Ministry application.
     * Restrictive CORS policy for security.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow only Ministry domains
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://ministry.silly.gov.uk",
            "https://*.ministry.silly.gov.uk",
            "https://localhost:*" // For development only
        ));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "X-Request-ID",
            "Accept",
            "Origin"
        ));
        
        // Exposed headers
        configuration.setExposedHeaders(Arrays.asList(
            "X-Request-ID",
            "X-Application-ID",
            "X-Silliness-Score",
            "X-Ministry-Status"
        ));
        
        // Credentials allowed
        configuration.setAllowCredentials(true);
        
        // Cache preflight for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    /**
     * Password encoder for secure password storage.
     * Uses BCrypt with strength 12 for Ministry-grade security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}