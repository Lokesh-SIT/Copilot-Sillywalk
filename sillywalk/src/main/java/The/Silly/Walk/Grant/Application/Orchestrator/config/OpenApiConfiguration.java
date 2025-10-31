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
                .description("## Overview\n" +
                    "The Ministry of Silly Walks Grant Application System API provides secure endpoints " +
                    "for submitting and managing grant applications for silly walk assessments.\n\n" +
                    "## Security\n" +
                    "This API implements enterprise-grade security measures:\n" +
                    "- JWT-based authentication required for all submission endpoints\n" +
                    "- Comprehensive input validation and sanitization\n" +
                    "- Rate limiting and fraud detection\n" +
                    "- Non-revealing error messages for security\n\n" +
                    "## Silliness Assessment\n" +
                    "Applications are automatically assessed using the Ministry's proprietary algorithm:\n" +
                    "- Base score: 10 points\n" +
                    "- Briefcase integration: +20 points\n" +
                    "- Hopping dynamics: +25 points\n" +  
                    "- Twirl complexity: +5 points per twirl\n" +
                    "- Description creativity: +10-40 points\n" +
                    "- Safety deductions: -5 to -15 points\n\n" +
                    "## Business Rules\n" +
                    "- Maximum 3 applications per applicant per 30-day period\n" +
                    "- Unique walk names required per applicant\n" +
                    "- Minimum description length: 50 characters\n" +
                    "- Maximum twirl count: 100 (safety consideration)\n\n" +
                    "## Support\n" +
                    "For technical support, contact the Ministry's IT department with the X-Request-ID " +
                    "header value from any API response.")
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
                .description("JWT Authentication required for secure endpoints.\n\n" +
                    "**How to obtain a token:**\n" +
                    "1. Contact Ministry IT for API credentials\n" +
                    "2. Use the authentication endpoint (when implemented)\n" +
                    "3. Include the token in the Authorization header: `Bearer <token>`\n\n" +
                    "**Token expiration:** 5 hours for access tokens\n" +
                    "**Refresh tokens:** 7 days validity\n\n" +
                    "**Required permissions:**\n" +
                    "- `USER`: Submit grant applications\n" +
                    "- `MINISTRY_STAFF`: View statistics and advanced features\n" +
                    "- `ADMIN`: Full system access");
    }
}