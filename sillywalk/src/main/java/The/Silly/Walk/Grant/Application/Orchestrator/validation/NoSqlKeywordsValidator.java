package The.Silly.Walk.Grant.Application.Orchestrator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator implementation for the @NoSqlKeywords annotation.
 * Prevents SQL injection attacks by detecting dangerous SQL patterns.
 */
public class NoSqlKeywordsValidator implements ConstraintValidator<NoSqlKeywords, String> {

    // Common SQL injection keywords and patterns
    private static final String[] SQL_KEYWORDS = {
        "select", "insert", "update", "delete", "drop", "create", "alter", "truncate",
        "union", "join", "where", "having", "group by", "order by",
        "exec", "execute", "sp_", "xp_", "sp_executesql",
        "script", "declare", "cast", "convert", "char", "nchar",
        "varchar", "nvarchar", "table", "database", "schema",
        "information_schema", "sys.", "sysobjects", "syscolumns"
    };

    // SQL injection pattern detection
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)('\\s*(or|and)\\s*')|" +                   // Basic OR/AND injection
        "(?i)(--|#|/\\*|\\*/)|" +                       // SQL comments
        "(?i)(;\\s*(drop|delete|insert|update))|" +     // Statement termination + dangerous command
        "(?i)(union\\s+select)|" +                      // UNION SELECT attacks
        "(?i)(1\\s*=\\s*1)|" +                         // Always true conditions
        "(?i)('\\s*;)|" +                              // Statement termination
        "(?i)(0x[0-9a-f]+)|" +                         // Hexadecimal values
        "(?i)(waitfor\\s+delay)|" +                     // Time-based attacks
        "(?i)(benchmark\\s*\\()|" +                     // MySQL benchmark attacks
        "(?i)(sleep\\s*\\()|" +                        // Sleep-based attacks
        "(?i)(load_file\\s*\\()|" +                     // File reading functions
        "(?i)(into\\s+outfile)|" +                      // File writing
        "(?i)(concat\\s*\\(.*select)|" +               // Concatenation with SELECT
        "(?i)(char\\s*\\()|" +                         // Character function attacks
        "(?i)(ascii\\s*\\()|" +                        // ASCII function attacks
        "(?i)(substring\\s*\\(.*select)"               // Substring with SELECT
    );

    // SQL function patterns that could be dangerous
    private static final Pattern SQL_FUNCTION_PATTERN = Pattern.compile(
        "(?i)(count\\s*\\(.*\\*)|" +
        "(?i)(len\\s*\\()|" +
        "(?i)(version\\s*\\()|" +
        "(?i)(user\\s*\\()|" +
        "(?i)(database\\s*\\()|" +
        "(?i)(@@version)|" +
        "(?i)(@@servername)|" +
        "(?i)(current_user)"
    );

    @Override
    public void initialize(NoSqlKeywords constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null values
        }

        String lowerValue = value.toLowerCase().trim();

        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(lowerValue).find()) {
            addCustomMessage(context, "SQL injection pattern detected");
            return false;
        }

        // Check for dangerous SQL functions
        if (SQL_FUNCTION_PATTERN.matcher(lowerValue).find()) {
            addCustomMessage(context, "Dangerous SQL function detected");
            return false;
        }

        // Check for SQL keywords in suspicious contexts
        if (containsSuspiciousSqlKeywords(lowerValue, context)) {
            return false;
        }

        // Check for encoded SQL injection attempts
        if (containsEncodedSqlPatterns(value, context)) {
            return false;
        }

        return true;
    }

    /**
     * Check for SQL keywords in suspicious contexts.
     */
    private boolean containsSuspiciousSqlKeywords(String lowerValue, ConstraintValidatorContext context) {
        for (String keyword : SQL_KEYWORDS) {
            if (lowerValue.contains(keyword)) {
                // Check if keyword appears in suspicious context
                if (isInSuspiciousContext(lowerValue, keyword)) {
                    addCustomMessage(context, "SQL keyword in suspicious context: " + keyword);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine if a SQL keyword appears in a suspicious context.
     */
    private boolean isInSuspiciousContext(String value, String keyword) {
        int keywordIndex = value.indexOf(keyword);
        if (keywordIndex == -1) return false;

        // Check characters around the keyword
        String beforeKeyword = keywordIndex > 5 ? 
            value.substring(keywordIndex - 5, keywordIndex) : 
            value.substring(0, keywordIndex);
        
        String afterKeyword = keywordIndex + keyword.length() + 5 < value.length() ?
            value.substring(keywordIndex + keyword.length(), keywordIndex + keyword.length() + 5) :
            value.substring(keywordIndex + keyword.length());

        // Suspicious patterns around SQL keywords
        String[] suspiciousPatterns = {
            "'", "\"", ";", "=", " or ", " and ", " union ", "--", "/*", "*/"
        };

        for (String pattern : suspiciousPatterns) {
            if (beforeKeyword.contains(pattern) || afterKeyword.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check for URL/HTML encoded SQL injection attempts.
     */
    private boolean containsEncodedSqlPatterns(String value, ConstraintValidatorContext context) {
        // Common URL encodings for SQL injection characters
        String[] encodedPatterns = {
            "%27", "%22", "%3B", "%20or%20", "%20and%20", "%20union%20",
            "%2D%2D", "%2F%2A", "%2A%2F", "%3D", "&#39;", "&#34;", "&#59;"
        };

        String lowerValue = value.toLowerCase();
        for (String pattern : encodedPatterns) {
            if (lowerValue.contains(pattern.toLowerCase())) {
                addCustomMessage(context, "Encoded SQL injection pattern detected");
                return true;
            }
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
        // Logger.security("SQL injection validation failed: " + technicalMessage);
    }
}