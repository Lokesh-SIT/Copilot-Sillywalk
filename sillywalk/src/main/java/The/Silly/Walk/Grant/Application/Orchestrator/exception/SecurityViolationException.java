package The.Silly.Walk.Grant.Application.Orchestrator.exception;

/**
 * Exception thrown when security violations are detected in grant applications.
 * This is part of the Ministry's first line of defense against malicious submissions.
 */
public class SecurityViolationException extends RuntimeException {

    private final String violationType;

    public SecurityViolationException(String message, String violationType) {
        super(message);
        this.violationType = violationType;
    }

    public SecurityViolationException(String message, String violationType, Throwable cause) {
        super(message, cause);
        this.violationType = violationType;
    }

    public String getViolationType() {
        return violationType;
    }
}