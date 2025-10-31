package The.Silly.Walk.Grant.Application.Orchestrator.mcp.providers;

import The.Silly.Walk.Grant.Application.Orchestrator.entities.GrantApplication;
import The.Silly.Walk.Grant.Application.Orchestrator.repositories.GrantApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MCP Resource Provider for Ministry of Silly Walks Grant Applications.
 * Provides access to grant application data through the MCP protocol.
 */
@Component
public class McpResourceProvider {

    private final GrantApplicationRepository grantApplicationRepository;

    @Autowired
    public McpResourceProvider(GrantApplicationRepository grantApplicationRepository) {
        this.grantApplicationRepository = grantApplicationRepository;
    }

    /**
     * List all available resources.
     */
    public List<Map<String, Object>> listResources() {
        List<GrantApplication> applications = grantApplicationRepository.findAll();
        
        return applications.stream()
                .map(this::createResourceMetadata)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific resource by URI.
     */
    public Optional<Map<String, Object>> getResource(String uri) {
        if (!uri.startsWith("grant://application/")) {
            return Optional.empty();
        }
        
        try {
            String idStr = uri.substring("grant://application/".length());
            Long id = Long.parseLong(idStr);
            
            Optional<GrantApplication> application = grantApplicationRepository.findById(id);
            if (application.isPresent()) {
                return Optional.of(createResourceContent(application.get()));
            }
        } catch (NumberFormatException e) {
            // Invalid ID format
        }
        
        return Optional.empty();
    }

    /**
     * Get resource by applicant name.
     */
    public List<Map<String, Object>> getResourcesByApplicant(String applicantName) {
        List<GrantApplication> applications = grantApplicationRepository.findByApplicantName(applicantName);
        
        return applications.stream()
                .map(this::createResourceContent)
                .collect(Collectors.toList());
    }

    /**
     * Get applications with silliness score above threshold.
     */
    public List<Map<String, Object>> getResourcesBySilliness(Integer minScore) {
        List<GrantApplication> applications = grantApplicationRepository.findBySillinessScoreGreaterThanEqual(minScore);
        
        return applications.stream()
                .map(this::createResourceContent)
                .collect(Collectors.toList());
    }

    /**
     * Create resource metadata for listing.
     */
    private Map<String, Object> createResourceMetadata(GrantApplication application) {
        return Map.of(
            "uri", "grant://application/" + application.getId(),
            "name", "Grant Application: " + application.getWalkName(),
            "description", "Silly walk grant application by " + application.getApplicantName() + 
                          " (Score: " + application.getSillinessScore() + ")",
            "mimeType", "application/json"
        );
    }

    /**
     * Create full resource content.
     */
    private Map<String, Object> createResourceContent(GrantApplication application) {
        return Map.of(
            "uri", "grant://application/" + application.getId(),
            "mimeType", "application/json",
            "text", formatApplicationAsText(application)
        );
    }

    /**
     * Format grant application as readable text.
     */
    private String formatApplicationAsText(GrantApplication application) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== Ministry of Silly Walks Grant Application ===\n\n");
        sb.append("Application ID: ").append(application.getId()).append("\n");
        sb.append("Applicant Name: ").append(application.getApplicantName()).append("\n");
        sb.append("Walk Name: ").append(application.getWalkName()).append("\n");
        sb.append("Submitted: ").append(application.getSubmittedAt()).append("\n\n");
        
        sb.append("=== Walk Details ===\n");
        sb.append("Description: ").append(application.getWalkDescription()).append("\n\n");
        
        sb.append("=== Assessment ===\n");
        sb.append("Uses Briefcase: ").append(application.getUsesBriefcase() ? "Yes" : "No").append("\n");
        sb.append("Includes Hopping: ").append(application.getIncludesHopping() ? "Yes" : "No").append("\n");
        sb.append("Number of Twirls: ").append(application.getNumberOfTwirls()).append("\n");
        sb.append("Potentially Dangerous: ").append(application.getIsPotentiallyDangerous() ? "Yes" : "No").append("\n\n");
        
        sb.append("=== Scoring ===\n");
        sb.append("Silliness Score: ").append(application.getSillinessScore()).append(" / 120\n");
        sb.append("Status: ").append(application.getStatus()).append("\n");
        
        if (application.getRejectionReason() != null) {
            sb.append("Rejection Reason: ").append(application.getRejectionReason()).append("\n");
        }
        
        sb.append("\n=== Ministry Assessment ===\n");
        sb.append(generateAssessmentText(application));
        
        return sb.toString();
    }

    /**
     * Generate assessment text based on silliness score.
     */
    private String generateAssessmentText(GrantApplication application) {
        int score = application.getSillinessScore();
        
        if (score >= 100) {
            return "EXCEPTIONAL SILLINESS: This walk demonstrates extraordinary commitment to the " +
                   "principles of silly locomotion. The Ministry commends the applicant's dedication " +
                   "to advancing the art of silly walking.";
        } else if (score >= 80) {
            return "HIGHLY COMMENDABLE: A well-executed silly walk with strong fundamentals. " +
                   "Shows promise for advancement in the Ministry's silly walking programs.";
        } else if (score >= 60) {
            return "SATISFACTORY SILLINESS: Meets the basic requirements for a silly walk. " +
                   "Some refinement needed but shows understanding of core principles.";
        } else if (score >= 40) {
            return "NEEDS IMPROVEMENT: While the attempt shows effort, significant work is needed " +
                   "to meet Ministry standards for proper silly walking technique.";
        } else {
            return "INSUFFICIENT SILLINESS: This application does not meet the minimum requirements " +
                   "for a silly walk grant. Consider studying approved techniques before reapplying.";
        }
    }
}