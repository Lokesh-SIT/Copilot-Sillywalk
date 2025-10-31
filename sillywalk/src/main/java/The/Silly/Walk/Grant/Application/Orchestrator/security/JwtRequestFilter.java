package The.Silly.Walk.Grant.Application.Orchestrator.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Request Filter for the Ministry of Silly Walks.
 * Validates JWT tokens and sets up Spring Security context.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

        // Extract JWT token from request
        String jwtToken = extractTokenFromRequest(request);
        String username = null;

        // Validate and process JWT token
        if (jwtToken != null) {
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                securityLogger.warn("JWT token validation failed: {} for IP: {}", 
                                  e.getMessage(), getClientIpAddress(request));
            }
        }

        // Set up authentication context if token is valid
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            if (jwtTokenUtil.isValidToken(jwtToken)) {
                UserDetails userDetails = createUserDetailsFromToken(jwtToken, username);
                
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("JWT authentication successful for user: {}", username);
                } else {
                    securityLogger.warn("JWT token validation failed for user: {} from IP: {}", 
                                      username, getClientIpAddress(request));
                }
            } else {
                securityLogger.warn("Invalid JWT token structure for user: {} from IP: {}", 
                                  username, getClientIpAddress(request));
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }

    /**
     * Create UserDetails object from JWT token information.
     */
    private UserDetails createUserDetailsFromToken(String token, String username) {
        try {
            String[] roles = jwtTokenUtil.getRolesFromToken(token);
            List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

            return User.builder()
                .username(username)
                .password("") // Password not needed for JWT authentication
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        } catch (Exception e) {
            logger.warn("Error creating UserDetails from token for user: {}", username);
            // Return minimal user details as fallback
            return User.builder()
                .username(username)
                .password("")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
        }
    }

    /**
     * Extract client IP address considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Determine if the request should skip JWT processing.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip JWT processing for public endpoints
        return path.equals("/api/v1/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/actuator/health") ||
               path.equals("/api/v1/auth/login");
    }
}