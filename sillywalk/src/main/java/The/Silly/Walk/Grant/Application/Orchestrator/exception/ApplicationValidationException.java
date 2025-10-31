package The.Silly.Walk.Grant.Application.Orchestrator.exception;

/**
 * Exception thrown when application validation fails according to Ministry business rules.
 * Used for non-security related validation failures.
 */
public class ApplicationValidationException extends RuntimeException {

    public ApplicationValidationException(String message) {
        super(message);
    }

    public ApplicationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}