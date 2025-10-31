package The.Silly.Walk.Grant.Application.Orchestrator.mcp.tools;

import The.Silly.Walk.Grant.Application.Orchestrator.dto.ApplicationSubmissionRequest;
import The.Silly.Walk.Grant.Application.Orchestrator.dto.ApplicationSubmissionResponse;
import The.Silly.Walk.Grant.Application.Orchestrator.entities.GrantApplication;
import The.Silly.Walk.Grant.Application.Orchestrator.repositories.GrantApplicationRepository;
import The.Silly.Walk.Grant.Application.Orchestrator.services.GrantApplicationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Tools implementation for Ministry of Silly Walks Grant Application System.
 * Provides tools that can be invoked by MCP clients to interact with the system.
 */
@Component
public class McpToolsProvider {

    private final GrantApplicationService grantApplicationService;
    private final GrantApplicationRepository grantApplicationRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public McpToolsProvider(GrantApplicationService grantApplicationService,
                           GrantApplicationRepository grantApplicationRepository,
                           ObjectMapper objectMapper) {
        this.grantApplicationService = grantApplicationService;
        this.grantApplicationRepository = grantApplicationRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all available tools.
     */
    public List<Map<String, Object>> getTools() {
        return List.of(
            createSubmitApplicationTool(),
            createGetApplicationTool(),
            createListApplicationsTool(),
            createGetStatisticsTool(),
            createSearchApplicationsTool()
        );
    }

    /**
     * Execute a tool with given arguments.
     */
    public Map<String, Object> executeTool(String toolName, JsonNode arguments) {
        try {
            return switch (toolName) {
                case "submit_grant_application" -> executeSubmitApplication(arguments);
                case "get_grant_application" -> executeGetApplication(arguments);
                case "list_grant_applications" -> executeListApplications(arguments);
                case "get_grant_statistics" -> executeGetStatistics(arguments);
                case "search_grant_applications" -> executeSearchApplications(arguments);
                default -> createErrorResult("Unknown tool: " + toolName);
            };
        } catch (Exception e) {
            return createErrorResult("Tool execution failed: " + e.getMessage());
        }
    }

    /**
     * Submit Grant Application Tool Definition.
     */
    private Map<String, Object> createSubmitApplicationTool() {
        return Map.of(
            "name", "submit_grant_application",
            "description", "Submit a new silly walk grant application to the Ministry of Silly Walks",
            "inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "applicantName", Map.of(
                        "type", "string",
                        "description", "Name of the applicant (3-100 characters)",
                        "minLength", 3,
                        "maxLength", 100
                    ),
                    "walkName", Map.of(
                        "type", "string", 
                        "description", "Name of the silly walk (3-100 characters)",
                        "minLength", 3,
                        "maxLength", 100
                    ),
                    "walkDescription", Map.of(
                        "type", "string",
                        "description", "Detailed description of the silly walk (50-2000 characters)",
                        "minLength", 50,
                        "maxLength", 2000
                    ),
                    "usesBriefcase", Map.of(
                        "type", "boolean",
                        "description", "Whether the walk incorporates a briefcase (+20 points)"
                    ),
                    "includesHopping", Map.of(
                        "type", "boolean", 
                        "description", "Whether the walk includes hopping movements (+25 points)"
                    ),
                    "numberOfTwirls", Map.of(
                        "type", "integer",
                        "description", "Number of twirls in the walk (0-100, +5 points each)",
                        "minimum", 0,
                        "maximum", 100
                    ),
                    "isPotentiallyDangerous", Map.of(
                        "type", "boolean",
                        "description", "Whether the walk might be dangerous (-5 to -15 points)"
                    )
                ),
                "required", List.of("applicantName", "walkName", "walkDescription", 
                                  "usesBriefcase", "includesHopping", "numberOfTwirls", 
                                  "isPotentiallyDangerous")
            )
        );
    }

    /**
     * Get Grant Application Tool Definition.
     */
    private Map<String, Object> createGetApplicationTool() {
        return Map.of(
            "name", "get_grant_application",
            "description", "Retrieve a specific grant application by ID",
            "inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "applicationId", Map.of(
                        "type", "integer",
                        "description", "ID of the grant application to retrieve"
                    )
                ),
                "required", List.of("applicationId")
            )
        );
    }

    /**
     * List Grant Applications Tool Definition.
     */
    private Map<String, Object> createListApplicationsTool() {
        return Map.of(
            "name", "list_grant_applications",
            "description", "List grant applications with optional filtering",
            "inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "limit", Map.of(
                        "type", "integer",
                        "description", "Maximum number of applications to return (default 10)",
                        "minimum", 1,
                        "maximum", 100
                    ),
                    "applicantName", Map.of(
                        "type", "string",
                        "description", "Filter by applicant name (optional)"
                    ),
                    "minScore", Map.of(
                        "type", "integer", 
                        "description", "Minimum silliness score filter (optional)",
                        "minimum", 0,
                        "maximum", 120
                    )
                )
            )
        );
    }

    /**
     * Get Statistics Tool Definition.
     */
    private Map<String, Object> createGetStatisticsTool() {
        return Map.of(
            "name", "get_grant_statistics",
            "description", "Get statistics about grant applications",
            "inputSchema", Map.of(
                "type", "object",
                "properties", Map.of()
            )
        );
    }

    /**
     * Search Applications Tool Definition.
     */
    private Map<String, Object> createSearchApplicationsTool() {
        return Map.of(
            "name", "search_grant_applications",
            "description", "Search grant applications by various criteria",
            "inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "query", Map.of(
                        "type", "string",
                        "description", "Search query for walk names or descriptions"
                    ),
                    "status", Map.of(
                        "type", "string",
                        "description", "Filter by application status",
                        "enum", List.of("PENDING", "APPROVED", "REJECTED")
                    )
                ),
                "required", List.of("query")
            )
        );
    }

    /**
     * Execute Submit Application Tool.
     */
    private Map<String, Object> executeSubmitApplication(JsonNode arguments) {
        try {
            ApplicationSubmissionRequest request = objectMapper.treeToValue(arguments, ApplicationSubmissionRequest.class);
            ApplicationSubmissionResponse response = grantApplicationService.submitApplication(request, "127.0.0.1");
            
            return Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", "Grant application submitted successfully!\n\n" +
                           "Application ID: " + response.getApplicationId() + "\n" +
                           "Applicant: " + response.getApplicantName() + "\n" +
                           "Walk Name: " + response.getWalkName() + "\n" +
                           "Silliness Score: " + response.getSillinessScore() + " / 120\n" +
                           "Status: " + response.getStatus() + "\n" +
                           "Submitted: " + response.getSubmittedAt() + "\n\n" +
                           "Assessment: " + generateScoreAnalysis(response.getSillinessScore())
                ))
            );
        } catch (Exception e) {
            return createErrorResult("Failed to submit application: " + e.getMessage());
        }
    }

    /**
     * Execute Get Application Tool.
     */
    private Map<String, Object> executeGetApplication(JsonNode arguments) {
        Long applicationId = arguments.get("applicationId").asLong();
        
        return grantApplicationRepository.findById(applicationId)
            .map(app -> Map.of(
                "content", List.of(Map.of(
                    "type", "text",
                    "text", formatApplicationDetails(app)
                ))
            ))
            .orElse(createErrorResult("Application not found with ID: " + applicationId));
    }

    /**
     * Execute List Applications Tool.
     */
    private Map<String, Object> executeListApplications(JsonNode arguments) {
        int limit = arguments.has("limit") ? arguments.get("limit").asInt() : 10;
        String applicantName = arguments.has("applicantName") ? arguments.get("applicantName").asText() : null;
        Integer minScore = arguments.has("minScore") ? arguments.get("minScore").asInt() : null;
        
        List<GrantApplication> applications;
        
        if (applicantName != null) {
            applications = grantApplicationRepository.findByApplicantName(applicantName);
        } else if (minScore != null) {
            applications = grantApplicationRepository.findBySillinessScoreGreaterThanEqual(minScore);
        } else {
            applications = grantApplicationRepository.findAll();
        }
        
        String result = applications.stream()
            .limit(limit)
            .map(this::formatApplicationSummary)
            .collect(Collectors.joining("\n\n"));
            
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "Grant Applications (" + Math.min(applications.size(), limit) + " shown):\n\n" + result
            ))
        );
    }

    /**
     * Execute Get Statistics Tool.
     */
    private Map<String, Object> executeGetStatistics(JsonNode arguments) {
        long totalApplications = grantApplicationRepository.count();
        double avgScore = grantApplicationRepository.findAverageSillinessScore();
        long highScoreApps = grantApplicationRepository.countBySillinessScoreGreaterThanEqual(80);
        
        String statsText = "Ministry of Silly Walks - Grant Application Statistics\n\n" +
                          "Total Applications: " + totalApplications + "\n" +
                          "Average Silliness Score: " + String.format("%.1f", avgScore) + " / 120\n" +
                          "High-Scoring Applications (80+): " + highScoreApps + "\n" +
                          "Excellence Rate: " + String.format("%.1f%%", (double) highScoreApps / totalApplications * 100);
        
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", statsText
            ))
        );
    }

    /**
     * Execute Search Applications Tool.
     */
    private Map<String, Object> executeSearchApplications(JsonNode arguments) {
        String query = arguments.get("query").asText().toLowerCase();
        String status = arguments.has("status") ? arguments.get("status").asText() : null;
        
        List<GrantApplication> applications = grantApplicationRepository.findAll().stream()
            .filter(app -> app.getWalkName().toLowerCase().contains(query) || 
                          app.getWalkDescription().toLowerCase().contains(query))
            .filter(app -> status == null || app.getStatus().toString().equals(status))
            .collect(Collectors.toList());
        
        String result = applications.stream()
            .map(this::formatApplicationSummary)
            .collect(Collectors.joining("\n\n"));
            
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "Search Results (" + applications.size() + " found):\n\n" + result
            ))
        );
    }

    /**
     * Create error result.
     */
    private Map<String, Object> createErrorResult(String message) {
        return Map.of(
            "content", List.of(Map.of(
                "type", "text",
                "text", "Error: " + message
            )),
            "isError", true
        );
    }

    /**
     * Format application details.
     */
    private String formatApplicationDetails(GrantApplication app) {
        return "=== Grant Application #" + app.getId() + " ===\n\n" +
               "Applicant: " + app.getApplicantName() + "\n" +
               "Walk Name: " + app.getWalkName() + "\n" +
               "Description: " + app.getWalkDescription() + "\n\n" +
               "Walk Features:\n" +
               "- Uses Briefcase: " + (app.getUsesBriefcase() ? "Yes" : "No") + "\n" +
               "- Includes Hopping: " + (app.getIncludesHopping() ? "Yes" : "No") + "\n" +
               "- Number of Twirls: " + app.getNumberOfTwirls() + "\n" +
               "- Potentially Dangerous: " + (app.getIsPotentiallyDangerous() ? "Yes" : "No") + "\n\n" +
               "Assessment:\n" +
               "- Silliness Score: " + app.getSillinessScore() + " / 120\n" +
               "- Status: " + app.getStatus() + "\n" +
               "- Submitted: " + app.getSubmittedAt() + "\n\n" +
               generateScoreAnalysis(app.getSillinessScore());
    }

    /**
     * Format application summary.
     */
    private String formatApplicationSummary(GrantApplication app) {
        return "ID: " + app.getId() + " | " + app.getWalkName() + " by " + app.getApplicantName() + 
               " | Score: " + app.getSillinessScore() + " | Status: " + app.getStatus();
    }

    /**
     * Generate score analysis.
     */
    private String generateScoreAnalysis(Integer score) {
        if (score >= 100) {
            return "EXCEPTIONAL: This walk demonstrates extraordinary silliness!";
        } else if (score >= 80) {
            return "EXCELLENT: A highly commendable silly walk.";
        } else if (score >= 60) {
            return "GOOD: Meets Ministry standards for silly walking.";
        } else if (score >= 40) {
            return "FAIR: Some improvement needed.";
        } else {
            return "POOR: Significant work required to meet Ministry standards.";
        }
    }
}