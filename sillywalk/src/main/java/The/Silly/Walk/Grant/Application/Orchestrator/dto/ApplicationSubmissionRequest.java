package The.Silly.Walk.Grant.Application.Orchestrator.dto;

import The.Silly.Walk.Grant.Application.Orchestrator.validation.NoScriptTags;
import The.Silly.Walk.Grant.Application.Orchestrator.validation.NoSqlKeywords;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Data Transfer Object for grant application submissions to the Ministry of Silly Walks.
 * Implements comprehensive validation and security measures as the first line of defense.
 */
@Schema(description = "Request payload for submitting a new silly walk grant application")
public class ApplicationSubmissionRequest {

    @NotBlank(message = "Applicant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Invalid name format")
    @Schema(description = "Full name of the grant applicant", 
            example = "John Cleese",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String applicantName;

    @NotBlank(message = "Walk name is required")
    @Size(min = 3, max = 50, message = "Walk name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.!]+$", message = "Invalid walk name format")
    @Schema(description = "Creative name for the silly walk", 
            example = "The Briefcase Bounce",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String walkName;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 1000, message = "Description must be between 50 and 1000 characters")
    @NoScriptTags(message = "Description contains invalid content")
    @NoSqlKeywords(message = "Description contains invalid content")
    @Schema(description = "Detailed description of the walk's silliness and execution",
            example = "A magnificently absurd walk involving a leather briefcase swung in wide arcs while performing synchronized hops on alternating feet, culminating in a series of theatrical twirls that would make the Ministry proud.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "Briefcase field is required")
    @Schema(description = "Whether the walk incorporates a briefcase as a prop", 
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasBriefcase;

    @NotNull(message = "Hopping field is required")
    @Schema(description = "Whether the walk involves hopping movements", 
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean involvesHopping;

    @NotNull(message = "Number of twirls is required")
    @Min(value = 0, message = "Twirls cannot be negative")
    @Max(value = 100, message = "Twirls cannot exceed 100")
    @Schema(description = "Count of twirling movements incorporated in the walk", 
            example = "3",
            minimum = "0", 
            maximum = "100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer numberOfTwirls;

    // Default constructor
    public ApplicationSubmissionRequest() {}

    // Constructor with all fields
    public ApplicationSubmissionRequest(String applicantName, String walkName, String description,
                                      Boolean hasBriefcase, Boolean involvesHopping, Integer numberOfTwirls) {
        this.applicantName = applicantName;
        this.walkName = walkName;
        this.description = description;
        this.hasBriefcase = hasBriefcase;
        this.involvesHopping = involvesHopping;
        this.numberOfTwirls = numberOfTwirls;
    }

    // Security validation method
    @AssertTrue(message = "Request contains invalid data")
    private boolean isSecurityCompliant() {
        return !containsSecurityThreats();
    }

    /**
     * Comprehensive security threat detection.
     * Returns true if any security threats are detected.
     */
    private boolean containsSecurityThreats() {
        return hasScriptTags() || hasSqlPatterns() || hasInvalidCharacters();
    }

    /**
     * Check for script tag injection attempts in any string field.
     */
    private boolean hasScriptTags() {
        String[] fieldsToCheck = {applicantName, walkName, description};
        for (String field : fieldsToCheck) {
            if (field != null && field.toLowerCase().contains("<script")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for SQL injection patterns in string fields.
     */
    private boolean hasSqlPatterns() {
        String[] sqlKeywords = {"select", "insert", "update", "delete", "drop", "union", "exec"};
        String[] fieldsToCheck = {applicantName, walkName, description};
        
        for (String field : fieldsToCheck) {
            if (field != null) {
                String lowerField = field.toLowerCase();
                for (String keyword : sqlKeywords) {
                    if (lowerField.contains(keyword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check for invalid Unicode control characters or dangerous patterns.
     */
    private boolean hasInvalidCharacters() {
        String[] fieldsToCheck = {applicantName, walkName, description};
        for (String field : fieldsToCheck) {
            if (field != null) {
                // Check for control characters
                for (char c : field.toCharArray()) {
                    if (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t') {
                        return true;
                    }
                }
                // Check for potentially dangerous patterns
                if (field.contains("javascript:") || field.contains("data:")) {
                    return true;
                }
            }
        }
        return false;
    }

    // Getters and Setters
    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getWalkName() {
        return walkName;
    }

    public void setWalkName(String walkName) {
        this.walkName = walkName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getHasBriefcase() {
        return hasBriefcase;
    }

    public void setHasBriefcase(Boolean hasBriefcase) {
        this.hasBriefcase = hasBriefcase;
    }

    public Boolean getInvolvesHopping() {
        return involvesHopping;
    }

    public void setInvolvesHopping(Boolean involvesHopping) {
        this.involvesHopping = involvesHopping;
    }

    public Integer getNumberOfTwirls() {
        return numberOfTwirls;
    }

    public void setNumberOfTwirls(Integer numberOfTwirls) {
        this.numberOfTwirls = numberOfTwirls;
    }

    @Override
    public String toString() {
        return "ApplicationSubmissionRequest{" +
                "applicantName='" + applicantName + '\'' +
                ", walkName='" + walkName + '\'' +
                ", hasBriefcase=" + hasBriefcase +
                ", involvesHopping=" + involvesHopping +
                ", numberOfTwirls=" + numberOfTwirls +
                '}';
    }
}