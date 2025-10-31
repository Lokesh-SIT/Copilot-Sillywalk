package The.Silly.Walk.Grant.Application.Orchestrator.exception;

/**
 * Exception thrown when duplicate applications are detected.
 * Enforces the Ministry's business rule of unique walk names per applicant.
 */
public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String message) {
        super(message);
    }

    public DuplicateApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}