package The.Silly.Walk.Grant.Application.Orchestrator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for successful grant application submissions to the Ministry of Silly Walks.
 * Contains essential information about the processed application.
 */
@Schema(description = "Response returned after successful application submission")
public class ApplicationSubmissionResponse {

    @Schema(description = "Unique identifier for the submitted application",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID applicationId;

    @Schema(description = "Current status of the application",
            example = "submitted")
    private String status;

    @Schema(description = "Timestamp when the application was submitted",
            example = "2025-10-31T10:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "Initial silliness score calculated by the Ministry's assessment algorithm",
            example = "78", 
            minimum = "0", 
            maximum = "120")
    private Integer initialSillinessScore;

    @Schema(description = "Confirmation message for the applicant",
            example = "Application successfully submitted for preliminary review")
    private String message;

    @Schema(description = "Unique request identifier for tracking and support",
            example = "req_12345-67890-abcdef")
    private String requestId;

    // Default constructor
    public ApplicationSubmissionResponse() {}

    // Constructor with all fields
    public ApplicationSubmissionResponse(UUID applicationId, String status, LocalDateTime submittedAt,
                                       Integer initialSillinessScore, String message, String requestId) {
        this.applicationId = applicationId;
        this.status = status;
        this.submittedAt = submittedAt;
        this.initialSillinessScore = initialSillinessScore;
        this.message = message;
        this.requestId = requestId;
    }

    // Builder pattern for convenient object creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID applicationId;
        private String status;
        private LocalDateTime submittedAt;
        private Integer initialSillinessScore;
        private String message;
        private String requestId;

        public Builder applicationId(UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder submittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt;
            return this;
        }

        public Builder initialSillinessScore(Integer initialSillinessScore) {
            this.initialSillinessScore = initialSillinessScore;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ApplicationSubmissionResponse build() {
            return new ApplicationSubmissionResponse(applicationId, status, submittedAt,
                    initialSillinessScore, message, requestId);
        }
    }

    // Getters and Setters
    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getInitialSillinessScore() {
        return initialSillinessScore;
    }

    public void setInitialSillinessScore(Integer initialSillinessScore) {
        this.initialSillinessScore = initialSillinessScore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "ApplicationSubmissionResponse{" +
                "applicationId=" + applicationId +
                ", status='" + status + '\'' +
                ", submittedAt=" + submittedAt +
                ", initialSillinessScore=" + initialSillinessScore +
                ", message='" + message + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}