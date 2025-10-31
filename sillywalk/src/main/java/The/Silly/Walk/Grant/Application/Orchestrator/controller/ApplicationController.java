package The.Silly.Walk.Grant.Application.Orchestrator.controller;

import The.Silly.Walk.Grant.Application.Orchestrator.dto.ApplicationSubmissionRequest;
import The.Silly.Walk.Grant.Application.Orchestrator.dto.ApplicationSubmissionResponse;
import The.Silly.Walk.Grant.Application.Orchestrator.dto.ValidationErrorResponse;
import The.Silly.Walk.Grant.Application.Orchestrator.service.GrantApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for the Ministry of Silly Walks Grant Application System.
 * Implements secure API endpoints for submitting and managing grant applications.
 * Security and comprehensive documentation are paramount.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Grant Applications", 
     description = "API for submitting and managing silly walk grant applications to the Ministry of Silly Walks")
@CrossOrigin(origins = {"https://ministry.silly.gov.uk"}, // Only allow ministry domain
            methods = {RequestMethod.POST, RequestMethod.GET},
            allowedHeaders = {"Content-Type", "Authorization"},
            exposedHeaders = {"X-Request-ID"},
            maxAge = 3600)
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private final GrantApplicationService applicationService;

    @Autowired
    public ApplicationController(GrantApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Submit a new silly walk grant application.
     * This is the primary endpoint for grant submissions to the Ministry.
     */
    @PostMapping("/applications")
    @Operation(
        summary = "Submit a new silly walk grant application",
        description = "Accepts JSON application data, performs comprehensive validation and security checks, " +
                     "calculates initial silliness score, and submits for Ministry review. " +
                     "This endpoint implements rigorous security measures as the first line of defense."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Application successfully submitted for Ministry review",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApplicationSubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid application data - validation errors detected",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidationErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required - valid Ministry credentials needed"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access forbidden - insufficient permissions for application submission"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - duplicate application detected or submission limits exceeded"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded - too many requests from this source"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error - Ministry systems temporarily unavailable"
        )
    })
    public ResponseEntity<ApplicationSubmissionResponse> submitApplication(
            @Parameter(description = "Grant application submission data", required = true)
            @Valid @RequestBody ApplicationSubmissionRequest request,
            HttpServletRequest httpRequest) {

        // Generate unique request ID for tracking and support
        String requestId = applicationService.generateRequestId();
        
        // Extract client information for audit logging
        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Audit log the submission attempt
        auditLogger.info("Application submission attempt - RequestID: {}, IP: {}, UserAgent: {}, " +
                        "Applicant: {}, Walk: {}", 
                        requestId, clientIp, sanitizeForLogging(userAgent),
                        sanitizeForLogging(request.getApplicantName()),
                        sanitizeForLogging(request.getWalkName()));

        try {
            // Process the application through the service layer
            ApplicationSubmissionResponse response = applicationService.submitApplication(request, requestId);

            // Add custom headers for tracking
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("X-Request-ID", requestId)
                    .header("X-Application-ID", response.getApplicationId().toString())
                    .header("X-Silliness-Score", response.getInitialSillinessScore().toString())
                    .body(response);

        } catch (Exception e) {
            // Error handling is delegated to GlobalExceptionHandler
            // Log the error details for internal monitoring
            logger.error("Application submission failed - RequestID: {}, Error: {}", 
                        requestId, e.getMessage(), e);
            throw e; // Re-throw for GlobalExceptionHandler
        }
    }

    /**
     * Health check endpoint for Ministry system monitoring.
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check for the Ministry application system",
        description = "Returns system health status for monitoring and load balancer checks"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "System is healthy and operational"),
        @ApiResponse(responseCode = "503", description = "System is unhealthy or under maintenance")
    })
    public ResponseEntity<Object> healthCheck() {
        return ResponseEntity.ok()
                .header("X-Ministry-Status", "Operational")
                .header("X-Timestamp", String.valueOf(System.currentTimeMillis()))
                .body(new HealthResponse("UP", "Ministry of Silly Walks Application System"));
    }

    /**
     * Get application submission statistics (for authorized Ministry staff).
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get application submission statistics",
        description = "Returns aggregated statistics for Ministry dashboard and reporting"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions for statistics access")
    })
    public ResponseEntity<Object> getStatistics(
            @Parameter(description = "Number of days for statistics calculation", example = "30")
            @RequestParam(defaultValue = "30") int days,
            HttpServletRequest httpRequest) {

        String requestId = applicationService.generateRequestId();
        String clientIp = getClientIpAddress(httpRequest);

        auditLogger.info("Statistics request - RequestID: {}, IP: {}, Days: {}", 
                        requestId, clientIp, days);

        try {
            var statistics = applicationService.getApplicationStatistics(days);
            
            return ResponseEntity.ok()
                    .header("X-Request-ID", requestId)
                    .header("Cache-Control", "private, max-age=300") // 5 minute cache
                    .body(statistics);

        } catch (Exception e) {
            logger.error("Statistics request failed - RequestID: {}, Error: {}", 
                        requestId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extract client IP address considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (first one is the original client)
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Sanitize strings for logging to prevent log injection attacks.
     */
    private String sanitizeForLogging(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_")
                   .replaceAll("[\\p{Cntrl}]", "?")
                   .substring(0, Math.min(input.length(), 100));
    }

    /**
     * Simple health response DTO.
     */
    @Schema(description = "Health check response")
    public static class HealthResponse {
        @Schema(description = "System status", example = "UP")
        private String status;
        
        @Schema(description = "System description", example = "Ministry of Silly Walks Application System")
        private String description;

        public HealthResponse(String status, String description) {
            this.status = status;
            this.description = description;
        }

        // Getters
        public String getStatus() { return status; }
        public String getDescription() { return description; }

        // Setters
        public void setStatus(String status) { this.status = status; }
        public void setDescription(String description) { this.description = description; }
    }
}