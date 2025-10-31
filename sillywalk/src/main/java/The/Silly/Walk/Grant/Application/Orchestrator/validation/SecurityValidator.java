package The.Silly.Walk.Grant.Application.Orchestrator.validation;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Comprehensive security validator for the Ministry of Silly Walks.
 * Provides centralized security validation logic for content safety.
 */
@Component
public class SecurityValidator {

    // Consolidated patterns for efficient validation
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)<\\s*script[^>]*>.*?</\\s*script\\s*>|" +
        "(?i)<\\s*script[^>]*/>|" +
        "(?i)<\\s*script[^>]*>|" +
        "(?i)javascript\\s*:|" +
        "(?i)on\\w+\\s*=|" +
        "(?i)expression\\s*\\(|" +
        "(?i)vbscript\\s*:|" +
        "(?i)data\\s*:.*script"
    );

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)('\\s*(or|and)\\s*')|" +
        "(?i)(--|#|/\\*|\\*/)|" +
        "(?i)(;\\s*(drop|delete|insert|update))|" +
        "(?i)(union\\s+select)|" +
        "(?i)(1\\s*=\\s*1)|" +
        "(?i)('\\s*;)|" +
        "(?i)(waitfor\\s+delay)|" +
        "(?i)(benchmark\\s*\\()|" +
        "(?i)(sleep\\s*\\()"
    );

    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile(
        "[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]|" +  // Control characters
        "[\uFEFF\uFFFE\uFFFF]"                         // Unicode BOM and invalid chars
    );

    /**
     * Comprehensive security validation result.
     */
    public static class SecurityValidationResult {
        private final boolean isValid;
        private final String violationType;
        private final String technicalDetails;

        public SecurityValidationResult(boolean isValid, String violationType, String technicalDetails) {
            this.isValid = isValid;
            this.violationType = violationType;
            this.technicalDetails = technicalDetails;
        }

        public boolean isValid() { return isValid; }
        public String getViolationType() { return violationType; }
        public String getTechnicalDetails() { return technicalDetails; }

        public static SecurityValidationResult valid() {
            return new SecurityValidationResult(true, null, null);
        }

        public static SecurityValidationResult invalid(String violationType, String details) {
            return new SecurityValidationResult(false, violationType, details);
        }
    }

    /**
     * Character set enumeration for validation.
     */
    public enum CharacterSet {
        ALPHANUMERIC_SPACES("^[a-zA-Z0-9\\s]+$"),
        NAME_CHARACTERS("^[a-zA-Z\\s'-]+$"),
        DESCRIPTION_SAFE("^[a-zA-Z0-9\\s\\-\\.!,;:()]+$"),
        WALK_NAME_SAFE("^[a-zA-Z0-9\\s\\-\\.!]+$");

        private final Pattern pattern;

        CharacterSet(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        public boolean matches(String input) {
            return input != null && pattern.matcher(input).matches();
        }
    }

    /**
     * Validate content against XSS attack patterns.
     */
    public boolean containsScriptTags(String input) {
        if (input == null) return false;
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Validate content against SQL injection patterns.
     */
    public boolean containsSqlKeywords(String input) {
        if (input == null) return false;
        return SQL_INJECTION_PATTERN.matcher(input.toLowerCase()).find();
    }

    /**
     * Validate character set compliance.
     */
    public boolean isValidCharacterSet(String input, CharacterSet allowedSet) {
        if (input == null) return true;
        return allowedSet.matches(input);
    }

    /**
     * Comprehensive content safety validation.
     */
    public SecurityValidationResult validateContent(String input) {
        if (input == null) {
            return SecurityValidationResult.valid();
        }

        // Check for XSS patterns
        if (containsScriptTags(input)) {
            return SecurityValidationResult.invalid("XSS", "Script tag or JavaScript pattern detected");
        }

        // Check for SQL injection patterns
        if (containsSqlKeywords(input)) {
            return SecurityValidationResult.invalid("SQL_INJECTION", "SQL injection pattern detected");
        }

        // Check for dangerous control characters
        if (DANGEROUS_CHARS_PATTERN.matcher(input).find()) {
            return SecurityValidationResult.invalid("DANGEROUS_CHARS", "Dangerous control characters detected");
        }

        // Check for suspicious URL patterns
        if (containsSuspiciousUrls(input)) {
            return SecurityValidationResult.invalid("SUSPICIOUS_URL", "Potentially malicious URL detected");
        }

        // Check for excessive special characters (potential encoding attack)
        if (hasExcessiveSpecialChars(input)) {
            return SecurityValidationResult.invalid("ENCODING_ATTACK", "Excessive special character usage detected");
        }

        return SecurityValidationResult.valid();
    }

    /**
     * Detect suspicious URL patterns that could be used for attacks.
     */
    private boolean containsSuspiciousUrls(String input) {
        String lowerInput = input.toLowerCase();
        String[] suspiciousProtocols = {
            "javascript:", "data:", "vbscript:", "file:", "ftp://", "about:"
        };

        for (String protocol : suspiciousProtocols) {
            if (lowerInput.contains(protocol)) {
                return true;
            }
        }

        // Check for suspicious domain patterns
        return lowerInput.matches(".*\\b(bit\\.ly|tinyurl|t\\.co|goo\\.gl)/.*") ||
               lowerInput.contains("..") ||  // Directory traversal
               lowerInput.contains("%2e%2e"); // Encoded directory traversal
    }

    /**
     * Check for excessive special characters that might indicate encoding attacks.
     */
    private boolean hasExcessiveSpecialChars(String input) {
        if (input.length() < 10) return false; // Skip short inputs

        long specialCharCount = input.chars()
            .filter(ch -> ch == '&' || ch == '<' || ch == '>' || ch == '\'' || 
                         ch == '"' || ch == '%' || ch == ';' || ch == '=' || ch == '+')
            .count();

        // Flag if more than 15% of characters are special characters
        return specialCharCount > input.length() * 0.15;
    }

    /**
     * Validate multiple fields for security compliance.
     */
    public SecurityValidationResult validateMultipleFields(String... fields) {
        for (String field : fields) {
            SecurityValidationResult result = validateContent(field);
            if (!result.isValid()) {
                return result;
            }
        }
        return SecurityValidationResult.valid();
    }

    /**
     * Sanitize input by removing potentially dangerous characters.
     * Note: This should be used carefully and validation should still be primary defense.
     */
    public String sanitizeInput(String input) {
        if (input == null) return null;

        return input
            .replaceAll("(?i)<script[^>]*>.*?</script>", "") // Remove script tags
            .replaceAll("(?i)javascript:", "")               // Remove javascript: protocol
            .replaceAll("(?i)on\\w+\\s*=", "")              // Remove event handlers
            .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "") // Remove control chars
            .trim();
    }
}