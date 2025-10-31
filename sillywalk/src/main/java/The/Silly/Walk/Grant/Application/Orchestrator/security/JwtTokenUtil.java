package The.Silly.Walk.Grant.Application.Orchestrator.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT utility service for the Ministry of Silly Walks.
 * Handles JWT token creation, validation, and claims extraction
 * with enterprise-grade security standards.
 */
@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    // Token validity periods
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours
    public static final long JWT_REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days

    @Value("${jwt.secret:minIstryOfSillyWalksSecretKeyForJWTTokenGenerationAndValidation2025}")
    private String secret;

    @Value("${jwt.issuer:ministry-silly-walks}")
    private String issuer;

    /**
     * Get the signing key for JWT tokens.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Retrieve username from JWT token.
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Retrieve expiration date from JWT token.
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Retrieve specific claim from JWT token.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Retrieve all claims from JWT token.
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            securityLogger.warn("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if the token has expired.
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Generate token for user.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername(), JWT_TOKEN_VALIDITY);
    }

    /**
     * Generate refresh token for user.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), JWT_REFRESH_TOKEN_VALIDITY);
    }

    /**
     * Create JWT token with specified claims and validity.
     */
    private String createToken(Map<String, Object> claims, String subject, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity * 1000);

        try {
            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey(), Jwts.SIG.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("Error creating JWT token: {}", e.getMessage());
            throw new RuntimeException("Could not create JWT token", e);
        }
    }

    /**
     * Validate token against user details.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            securityLogger.warn("JWT validation failed for token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate token structure and signature.
     */
    public Boolean isValidToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            securityLogger.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user roles from token.
     */
    public String[] getRolesFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            @SuppressWarnings("unchecked")
            Object roles = claims.get("roles");
            if (roles instanceof String) {
                return new String[]{(String) roles};
            }
            // Handle complex role structures if needed
            return new String[]{"USER"}; // Default role
        } catch (Exception e) {
            logger.warn("Could not extract roles from token: {}", e.getMessage());
            return new String[]{"USER"};
        }
    }

    /**
     * Check if token is a refresh token.
     */
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get token type (access or refresh).
     */
    public String getTokenType(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Object type = claims.get("type");
            return type != null ? type.toString() : "access";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get remaining token validity in seconds.
     */
    public long getTokenValiditySeconds(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long now = System.currentTimeMillis();
            long remaining = expiration.getTime() - now;
            return Math.max(0, remaining / 1000);
        } catch (Exception e) {
            return 0;
        }
    }
}