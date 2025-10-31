package The.Silly.Walk.Grant.Application.Orchestrator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for the @NoScriptTags annotation.
 * Prevents XSS attacks by detecting and rejecting script tag patterns.
 */
public class NoScriptTagsValidator implements ConstraintValidator<NoScriptTags, String> {

    // Comprehensive pattern to detect various script tag formats
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "(?i)<\\s*script[^>]*>.*?</\\s*script\\s*>|" +  // Standard script tags
        "(?i)<\\s*script[^>]*/>|" +                      // Self-closing script tags
        "(?i)<\\s*script[^>]*>|" +                       // Opening script tags
        "(?i)javascript\\s*:|" +                         // JavaScript protocol
        "(?i)on\\w+\\s*=|" +                            // Event handlers
        "(?i)expression\\s*\\(|" +                       // CSS expressions
        "(?i)vbscript\\s*:|" +                          // VBScript protocol
        "(?i)data\\s*:.*script"                         // Data URLs with script
    );

    // Additional patterns for obfuscated attacks
    private static final Pattern OBFUSCATED_PATTERN = Pattern.compile(
        "(?i)&#x?[0-9a-f]+;|" +                        // HTML entities
        "(?i)\\\\u[0-9a-f]{4}|" +                       // Unicode escapes
        "(?i)eval\\s*\\(|" +                            // eval() calls
        "(?i)settimeout\\s*\\(|" +                      // setTimeout calls
        "(?i)setinterval\\s*\\("                        // setInterval calls
    );

    @Override
    public void initialize(NoScriptTags constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null values
        }

        // Check for script patterns
        if (SCRIPT_PATTERN.matcher(value).find()) {
            addCustomMessage(context, "Script tag detected in content");
            return false;
        }

        // Check for obfuscated patterns
        if (OBFUSCATED_PATTERN.matcher(value).find()) {
            addCustomMessage(context, "Potentially obfuscated script content detected");
            return false;
        }

        // Additional security checks
        return !containsSuspiciousContent(value, context);
    }

    /**
     * Check for additional suspicious content patterns.
     */
    private boolean containsSuspiciousContent(String value, ConstraintValidatorContext context) {
        String lowerValue = value.toLowerCase();

        // Check for suspicious keywords
        String[] suspiciousKeywords = {
            "alert(", "confirm(", "prompt(",
            "document.cookie", "window.location",
            "iframe", "embed", "object",
            ".innerhtml", ".outerhtml"
        };

        for (String keyword : suspiciousKeywords) {
            if (lowerValue.contains(keyword)) {
                addCustomMessage(context, "Suspicious JavaScript pattern detected");
                return true;
            }
        }

        // Check for excessive special characters (potential encoding attacks)
        long specialCharCount = value.chars()
            .filter(ch -> ch == '&' || ch == '<' || ch == '>' || ch == '\'' || ch == '"' || ch == '%')
            .count();
        
        if (specialCharCount > value.length() * 0.1) { // More than 10% special chars
            addCustomMessage(context, "Excessive special characters detected");
            return true;
        }

        return false;
    }

    /**
     * Add a custom validation message while maintaining security.
     * The actual message returned will be generic for security purposes.
     */
    private void addCustomMessage(ConstraintValidatorContext context, String technicalMessage) {
        context.disableDefaultConstraintViolation();
        // Return generic message for security - don't reveal specific attack vector
        context.buildConstraintViolationWithTemplate("Content contains invalid data")
                .addConstraintViolation();
        
        // Log the technical details for security monitoring (would be logged in real implementation)
        // Logger.security("Script tag validation failed: " + technicalMessage);
    }
}