package The.Silly.Walk.Grant.Application.Orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the Ministry of Silly Walks Grant Application System.
 * Provides comprehensive API documentation with security specifications.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .addSecurityItem(new SecurityRequirement().addList("JWT Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT Authentication", 
                                createJWTSecurityScheme()));
    }

    /**
     * API information for the Ministry of Silly Walks system.
     */
    private Info apiInfo() {
        return new Info()
                .title("Ministry of Silly Walks Grant Application API")
                .description("""
                    ## Overview
                    The Ministry of Silly Walks Grant Application System API provides secure endpoints 
                    for submitting and managing grant applications for silly walk assessments.
                    
                    ## Security
                    This API implements enterprise-grade security measures:
                    - JWT-based authentication required for all submission endpoints
                    - Comprehensive input validation and sanitization
                    - Rate limiting and fraud detection
                    - Non-revealing error messages for security
                    
                    ## Silliness Assessment
                    Applications are automatically assessed using the Ministry's proprietary algorithm:
                    - Base score: 10 points
                    - Briefcase integration: +20 points
                    - Hopping dynamics: +25 points  
                    - Twirl complexity: +5 points per twirl
                    - Description creativity: +10-40 points
                    - Safety deductions: -5 to -15 points
                    
                    ## Business Rules
                    - Maximum 3 applications per applicant per 30-day period
                    - Unique walk names required per applicant
                    - Minimum description length: 50 characters
                    - Maximum twirl count: 100 (safety consideration)
                    
                    ## Support
                    For technical support, contact the Ministry's IT department with the X-Request-ID 
                    header value from any API response.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Ministry of Silly Walks IT Department")
                        .email("it-support@ministry.silly.gov.uk")
                        .url("https://ministry.silly.gov.uk/support"))
                .license(new License()
                        .name("UK Government License")
                        .url("https://ministry.silly.gov.uk/license"));
    }

    /**
     * API server configurations.
     */
    private List<Server> apiServers() {
        return List.of(
                new Server()
                        .url("https://api.ministry.silly.gov.uk")
                        .description("Production Ministry Server"),
                new Server()
                        .url("https://staging-api.ministry.silly.gov.uk")
                        .description("Staging Ministry Server"),
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server")
        );
    }

    /**
     * JWT security scheme configuration.
     */
    private SecurityScheme createJWTSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                    JWT Authentication required for secure endpoints.
                    
                    **How to obtain a token:**
                    1. Contact Ministry IT for API credentials
                    2. Use the authentication endpoint (when implemented)
                    3. Include the token in the Authorization header: `Bearer <token>`
                    
                    **Token expiration:** 5 hours for access tokens
                    **Refresh tokens:** 7 days validity
                    
                    **Required permissions:**
                    - `USER`: Submit grant applications
                    - `MINISTRY_STAFF`: View statistics and advanced features
                    - `ADMIN`: Full system access
                    """);
    }
}