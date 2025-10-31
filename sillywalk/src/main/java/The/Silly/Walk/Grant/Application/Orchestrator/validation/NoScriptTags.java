package The.Silly.Walk.Grant.Application.Orchestrator.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to prevent XSS attacks via script tag injection.
 * Part of the Ministry's first line of defense against security threats.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoScriptTagsValidator.class)
@Documented
public @interface NoScriptTags {
    
    String message() default "Content contains potentially dangerous script tags";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}