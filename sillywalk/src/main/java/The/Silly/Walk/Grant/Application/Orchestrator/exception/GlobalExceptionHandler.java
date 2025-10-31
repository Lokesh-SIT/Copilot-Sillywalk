package The.Silly.Walk.Grant.Application.Orchestrator.exception;

import The.Silly.Walk.Grant.Application.Orchestrator.dto.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Global Exception Handler for the Ministry of Silly Walks Grant Application System.
 * Implements security-focused error handling with non-revealing messages
 * while maintaining comprehensive internal logging for security monitoring.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    /**
     * Handle security violations with generic responses.
     */
    @ExceptionHandler(SecurityViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleSecurityViolation(
            SecurityViolationException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        // Log detailed security information for monitoring
        securityLogger.error("SECURITY_VIOLATION - RequestID: {}, IP: {}, URI: {}, Type: {}, Message: {}",
                            requestId, clientIp, request.getRequestURI(), 
                            ex.getViolationType(), ex.getMessage());

        // Return generic, non-revealing error message
        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Request cannot be processed")
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Handle duplicate application submissions.
     */
    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ValidationErrorResponse> handleDuplicateApplication(
            DuplicateApplicationException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        logger.warn("DUPLICATE_APPLICATION - RequestID: {}, IP: {}, URI: {}, Message: {}",
                   requestId, clientIp, request.getRequestURI(), ex.getMessage());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message("Application already exists")
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Handle application validation failures.
     */
    @ExceptionHandler(ApplicationValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleApplicationValidation(
            ApplicationValidationException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        logger.warn("VALIDATION_FAILED - RequestID: {}, IP: {}, URI: {}, Message: {}",
                   requestId, clientIp, request.getRequestURI(), ex.getMessage());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request contains invalid data")
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Handle Spring validation errors (from @Valid annotations).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        logger.warn("METHOD_ARGUMENT_NOT_VALID - RequestID: {}, IP: {}, URI: {}, Errors: {}",
                   requestId, clientIp, request.getRequestURI(), ex.getBindingResult().getErrorCount());

        // Extract field errors but sanitize messages for security
        List<ValidationErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            String sanitizedMessage = sanitizeValidationMessage(error.getDefaultMessage());
            fieldErrors.add(new ValidationErrorResponse.FieldError(error.getField(), sanitizedMessage));
        }

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request contains invalid data")
                .fieldErrors(fieldErrors)
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Handle constraint violations (from bean validation).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        logger.warn("CONSTRAINT_VIOLATION - RequestID: {}, IP: {}, URI: {}, Violations: {}",
                   requestId, clientIp, request.getRequestURI(), ex.getConstraintViolations().size());

        // Extract constraint violations with sanitized messages
        List<ValidationErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String field = violation.getPropertyPath().toString();
            String sanitizedMessage = sanitizeValidationMessage(violation.getMessage());
            fieldErrors.add(new ValidationErrorResponse.FieldError(field, sanitizedMessage));
        }

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request contains invalid data")
                .fieldErrors(fieldErrors)
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ValidationErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        securityLogger.warn("ACCESS_DENIED - RequestID: {}, IP: {}, URI: {}, Message: {}",
                           requestId, clientIp, request.getRequestURI(), ex.getMessage());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Access denied")
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ValidationErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        logger.warn("ILLEGAL_ARGUMENT - RequestID: {}, IP: {}, URI: {}, Message: {}",
                   requestId, clientIp, request.getRequestURI(), ex.getMessage());

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message("Invalid request parameters")
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Handle all other exceptions with generic response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        String requestId = generateRequestId();
        String clientIp = getClientIpAddress(request);

        // Log full exception details for internal monitoring
        logger.error("INTERNAL_ERROR - RequestID: {}, IP: {}, URI: {}, Exception: {}",
                    requestId, clientIp, request.getRequestURI(), ex.getClass().getSimpleName(), ex);

        // Return generic error message to client
        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .requestId(requestId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Request-ID", requestId)
                .body(response);
    }

    /**
     * Sanitize validation messages to prevent information disclosure.
     */
    private String sanitizeValidationMessage(String message) {
        if (message == null) {
            return "Invalid data";
        }

        // Map specific validation messages to generic ones for security
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("script") || lowerMessage.contains("xss") || 
            lowerMessage.contains("injection") || lowerMessage.contains("sql")) {
            return "Invalid data format";
        }
        
        if (lowerMessage.contains("length") || lowerMessage.contains("size") || 
            lowerMessage.contains("characters")) {
            return extractLengthRequirement(message);
        }
        
        if (lowerMessage.contains("required") || lowerMessage.contains("blank") || 
            lowerMessage.contains("null")) {
            return "This field is required";
        }
        
        if (lowerMessage.contains("format") || lowerMessage.contains("pattern") || 
            lowerMessage.contains("invalid")) {
            return "Invalid format";
        }
        
        if (lowerMessage.contains("range") || lowerMessage.contains("min") || 
            lowerMessage.contains("max")) {
            return extractRangeRequirement(message);
        }

        // Return original message if it doesn't contain sensitive information
        return message.length() > 100 ? "Invalid data" : message;
    }

    /**
     * Extract safe length requirements from validation messages.
     */
    private String extractLengthRequirement(String message) {
        // Extract numeric values safely
        if (message.contains("between") && message.contains("and")) {
            return message; // Safe to return length requirements
        }
        if (message.matches(".*\\d+.*\\d+.*")) {
            return message; // Contains numbers, likely safe length info
        }
        return "Invalid length";
    }

    /**
     * Extract safe range requirements from validation messages.
     */
    private String extractRangeRequirement(String message) {
        if (message.matches(".*\\d+.*")) {
            return message; // Contains numbers, likely safe range info
        }
        return "Value out of range";
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
     * Generate unique request ID for tracking.
     */
    private String generateRequestId() {
        return "req_" + UUID.randomUUID().toString().substring(0, 8) + "_" + 
               System.currentTimeMillis();
    }
}