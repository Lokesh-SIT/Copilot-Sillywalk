package The.Silly.Walk.Grant.Application.Orchestrator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized error response DTO for validation failures and security violations.
 * Provides clear but non-revealing error messages as per security requirements.
 */
@Schema(description = "Error response for validation failures or security violations")
public class ValidationErrorResponse {

    @Schema(description = "Timestamp when the error occurred",
            example = "2025-10-31T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code",
            example = "400")
    private int status;

    @Schema(description = "HTTP error description",
            example = "Bad Request")
    private String error;

    @Schema(description = "General error message (non-revealing for security)",
            example = "Request contains invalid data")
    private String message;

    @Schema(description = "List of field-specific validation errors")
    private List<FieldError> fieldErrors;

    @Schema(description = "Request path that caused the error",
            example = "/api/v1/applications")
    private String path;

    @Schema(description = "Unique request identifier for tracking",
            example = "req_12345-67890-abcdef")
    private String requestId;

    // Default constructor
    public ValidationErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with common fields
    public ValidationErrorResponse(int status, String error, String message, String path, String requestId) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
    }

    /**
     * Field-specific validation error information.
     * Contains only safe, non-revealing error details.
     */
    @Schema(description = "Field-specific validation error")
    public static class FieldError {
        @Schema(description = "Name of the field that failed validation",
                example = "description")
        private String field;

        @Schema(description = "Non-revealing validation error message",
                example = "Description must be between 50 and 1000 characters")
        private String message;

        // Default constructor
        public FieldError() {}

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        // Getters and Setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "FieldError{" +
                    "field='" + field + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    // Builder pattern for convenient object creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int status;
        private String error;
        private String message;
        private List<FieldError> fieldErrors;
        private String path;
        private String requestId;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder fieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ValidationErrorResponse build() {
            ValidationErrorResponse response = new ValidationErrorResponse(status, error, message, path, requestId);
            response.setFieldErrors(fieldErrors);
            return response;
        }
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "ValidationErrorResponse{" +
                "timestamp=" + timestamp +
                ", status=" + status +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", fieldErrors=" + fieldErrors +
                ", path='" + path + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}