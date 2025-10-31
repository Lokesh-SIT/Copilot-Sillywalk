package The.Silly.Walk.Grant.Application.Orchestrator.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to prevent SQL injection attacks.
 * Detects and rejects common SQL keywords and injection patterns.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoSqlKeywordsValidator.class)
@Documented
public @interface NoSqlKeywords {
    
    String message() default "Content contains potentially dangerous SQL patterns";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}