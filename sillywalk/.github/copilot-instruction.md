# Copilot Instructions for Silly Walk Grant Application System

## Project Overview

This is the **Silly Walk Grant Application System** - a Spring Boot 17 backend service that automates the initial intake, validation, and preliminary "silliness assessment" of grant applications for the Ministry of Silly Walks. Only the silliest (and most securely processed and well-documented) walks receive due consideration for final review by the Grand Council of Gait.

## Project Context

- **Application Name**: Silly Walk Grant
- **Framework**: Spring Boot 3.5.7
- **Java Version**: 17
- **Package Structure**: `The.Silly.Walk.Grant.Application.Orchestrator`
- **Purpose**: Backend API service for internal ministerial use
- **Security Level**: Paramount importance
- **Documentation**: Comprehensive API documentation required

## Architecture & Design Principles

### Core Principles
1. **Silliness First**: All features should prioritize the assessment and categorization of walk silliness
2. **Security Paramount**: Implement robust security measures for sensitive grant data
3. **API-Driven**: RESTful API design with comprehensive OpenAPI/Swagger documentation
4. **Well-Documented**: Every endpoint, service, and data model must be thoroughly documented
5. **Testable**: High test coverage with unit, integration, and security tests

### Recommended Architecture
- **Controller Layer**: RESTful endpoints for grant management
- **Service Layer**: Business logic for silliness assessment and validation
- **Repository Layer**: Data persistence for grant applications
- **Security Layer**: Authentication, authorization, and data protection
- **Documentation Layer**: OpenAPI specifications and API documentation

## Development Guidelines

### Code Structure
```
src/main/java/The/Silly/Walk/Grant/Application/Orchestrator/
├── controller/          # REST controllers
├── service/            # Business logic services
├── repository/         # Data access layer
├── model/              # Entity models and DTOs
├── security/           # Security configurations
├── config/             # Application configurations
├── validation/         # Custom validators
└── exception/          # Exception handling
```

### Required Dependencies
When suggesting new dependencies, prioritize:
- **Spring Boot Security** for authentication/authorization
- **Spring Boot Data JPA** for database operations
- **Spring Boot Validation** for input validation
- **SpringDoc OpenAPI** for API documentation
- **Spring Boot Test** for comprehensive testing
- **H2/PostgreSQL** for database (suggest based on context)

### API Design Standards

#### Endpoint Naming Conventions
- Use descriptive, RESTful paths: `/api/v1/grant-applications`
- Include version in API paths: `/api/v1/`
- Use plural nouns for collections: `/applications`
- Use specific identifiers: `/applications/{applicationId}`

#### Response Formats
- Always return consistent JSON structures
- Include proper HTTP status codes
- Implement error response standards
- Add pagination for list endpoints

#### Required Endpoints (Suggest implementing)
```
POST   /api/v1/applications                 # Submit new application (PRIMARY ENDPOINT)
GET    /api/v1/applications                 # List applications (paginated)
GET    /api/v1/applications/{id}            # Get specific application
PUT    /api/v1/applications/{id}            # Update application
DELETE /api/v1/applications/{id}            # Delete application
POST   /api/v1/applications/{id}/assess     # Perform silliness assessment
GET    /api/v1/silliness-categories         # Get available silliness categories
```

#### Application Submission API Specification

**POST /api/v1/applications**

The primary endpoint for submitting new silly walk grant applications. This endpoint accepts JSON payloads containing all required application details and performs initial validation before storing the application for assessment.

**Request Body Structure:**
```json
{
  "applicant_name": "string",       // Required: Full name of the applicant
  "walk_name": "string",           // Required: Creative name for the silly walk
  "description": "string",         // Required: Detailed description of walk's silliness
  "has_briefcase": boolean,        // Required: Whether walk involves a briefcase
  "involves_hopping": boolean,     // Required: Whether walk includes hopping motions
  "number_of_twirls": integer     // Required: Count of twirling movements (0 or positive)
}
```

**Validation Rules:**
- `applicant_name`: 2-100 characters, letters and spaces only
- `walk_name`: 3-50 characters, alphanumeric and basic punctuation
- `description`: 50-1000 characters, detailed explanation required
- `has_briefcase`: Must be explicit boolean value
- `involves_hopping`: Must be explicit boolean value  
- `number_of_twirls`: Must be non-negative integer (0-100)

**Security-First Validation Requirements:**

*Input Sanitization & Type Validation:*
- All fields MUST pass type validation before processing
- String fields MUST be validated against allowed character sets
- Numeric fields MUST be validated for range and type safety
- Boolean fields MUST be explicit true/false values (no truthy/falsy)
- No field may contain null, undefined, or empty values where not explicitly allowed

*Character Set Restrictions:*
- `applicant_name`: Only letters (a-z, A-Z), spaces, hyphens, apostrophes
- `walk_name`: Alphanumeric, spaces, hyphens, periods, exclamation marks only
- `description`: Alphanumeric, common punctuation, no script tags or SQL keywords
- Reject any input containing: `<script>`, `javascript:`, SQL keywords, Unicode control characters

*Length & Format Enforcement:*
- Exact character count validation (not just min/max)
- Trim whitespace before validation but preserve internal spaces
- Reject excessively long inputs that could cause buffer overflows
- Validate UTF-8 encoding compliance

**Response Formats:**

*Success Response (201 Created):*
```json
{
  "application_id": "uuid",
  "status": "submitted",
  "submitted_at": "2025-10-31T10:30:00Z",
  "initial_silliness_score": 0,
  "message": "Application successfully submitted for preliminary review"
}
```

*Validation Error Response (400 Bad Request):*
```json
{
  "timestamp": "2025-10-31T10:30:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request contains invalid data",
  "field_errors": [
    {
      "field": "description",
      "message": "Description must be between 50 and 1000 characters"
    },
    {
      "field": "number_of_twirls", 
      "message": "Value must be a non-negative integer between 0 and 100"
    }
  ],
  "path": "/api/v1/applications",
  "request_id": "uuid-for-tracing"
}
```

**Security-Focused Error Handling:**
- Error messages MUST be informative but non-revealing
- NO internal system details, stack traces, or database schema information
- NO indication of which specific validation rule failed (prevents reconnaissance)
- Generic messages for security violations: "Invalid request format"
- Detailed field errors ONLY for format/length violations, not business logic
- All validation failures logged with request details for security monitoring

### Data Models

#### Core Entities to Implement
1. **GrantApplication**
   - application_id (UUID, primary key)
   - applicant_name (String, 2-100 chars)
   - walk_name (String, 3-50 chars)  
   - description (String, 50-1000 chars)
   - has_briefcase (Boolean, required)
   - involves_hopping (Boolean, required)
   - number_of_twirls (Integer, 0-100, required)
   - submitted_at (Timestamp)
   - status (Enum: SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED)
   - initial_silliness_score (Integer, calculated)

2. **SillinessAssessment**
   - Assessment criteria and scores
   - Automated assessment results based on application fields
   - Manual review notes
   - Final recommendation
   - Scoring algorithm using briefcase, hopping, and twirl factors

3. **WalkCharacteristics** 
   - Derived from application submission data
   - Briefcase integration factor
   - Hopping rhythm and style
   - Twirl complexity and frequency
   - Overall comedic composition

### Security Requirements

#### Authentication & Authorization
- Implement JWT-based authentication
- Role-based access control (RBAC)
- Secure API endpoints with proper authorization
- Rate limiting for API calls

#### Data Protection & Input Validation (CRITICAL SECURITY)
- **Rigorous Input Validation**: ALL incoming data MUST be validated against expected types, formats, lengths, and character sets BEFORE any processing
- **First Line of Defense**: Validation occurs immediately upon request receipt, before business logic execution
- **SQL Injection Prevention**: Parameterized queries and input sanitization
- **XSS Protection**: Output encoding and input filtering
- **Data Type Enforcement**: Strict type checking for all fields
- **Character Set Validation**: Whitelist-based character validation
- **Length Boundary Enforcement**: Hard limits on all string inputs
- **Secure Error Responses**: Clear but non-revealing error messages
- **Audit Logging**: All validation failures and security events logged

#### Security Headers
```java
// Suggest these security configurations
@EnableWebSecurity
@EnableMethodSecurity
// HTTPS enforcement
// CSRF protection for state-changing operations
// CORS configuration for approved origins
```

### Testing Standards

#### Test Coverage Requirements
- **Unit Tests**: Minimum 80% code coverage
- **Integration Tests**: All API endpoints
- **Security Tests**: Authentication and authorization
- **Performance Tests**: Load testing for critical endpoints

#### Test Categories
```java
// Unit Tests
@ExtendWith(MockitoExtension.class)
class SillinessAssessmentServiceTest

// Integration Tests  
@SpringBootTest
@AutoConfigureTestDatabase
class GrantApplicationControllerIntegrationTest

// Security Tests
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest
```

### Documentation Standards

#### Code Documentation
- JavaDoc for all public methods and classes
- Inline comments for complex business logic
- README updates for new features
- Architecture decision records (ADRs)

#### API Documentation
- Complete OpenAPI 3.0 specifications
- Request/response examples
- Error code documentation
- Authentication requirements
- Rate limiting information

#### Example OpenAPI Annotations
```java
@Operation(summary = "Submit a new silly walk grant application", 
           description = "Accepts JSON application data and performs initial validation and silliness assessment")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", 
                description = "Application successfully submitted",
                content = @Content(schema = @Schema(implementation = ApplicationSubmissionResponse.class))),
    @ApiResponse(responseCode = "400", 
                description = "Invalid application data - validation errors",
                content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))),
    @ApiResponse(responseCode = "401", 
                description = "Authentication required"),
    @ApiResponse(responseCode = "429", 
                description = "Rate limit exceeded - too many submissions")
})
@PostMapping("/api/v1/applications")
public ResponseEntity<ApplicationSubmissionResponse> submitApplication(
    @Valid @RequestBody ApplicationSubmissionRequest request) {
    // Implementation here
}
```

**Required DTO Classes:**
```java
// ApplicationSubmissionRequest.java
public class ApplicationSubmissionRequest {
    @NotBlank(message = "Applicant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Invalid name format")
    @Schema(description = "Full name of the grant applicant", example = "John Cleese")
    private String applicantName;
    
    @NotBlank(message = "Walk name is required")
    @Size(min = 3, max = 50, message = "Walk name must be between 3 and 50 characters")  
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.!]+$", message = "Invalid walk name format")
    @Schema(description = "Creative name for the silly walk", example = "The Briefcase Bounce")
    private String walkName;
    
    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 1000, message = "Description must be between 50 and 1000 characters")
    @NoScriptTags
    @NoSqlKeywords  
    @Schema(description = "Detailed description of the walk's silliness and execution")
    private String description;
    
    @NotNull(message = "Briefcase field is required")
    @Schema(description = "Whether the walk incorporates a briefcase", example = "true")
    private Boolean hasBriefcase;
    
    @NotNull(message = "Hopping field is required")
    @Schema(description = "Whether the walk involves hopping movements", example = "false")
    private Boolean involvesHopping;
    
    @NotNull(message = "Number of twirls is required")
    @Min(value = 0, message = "Twirls cannot be negative")
    @Max(value = 100, message = "Twirls cannot exceed 100")
    @Schema(description = "Count of twirling movements in the walk", example = "3")
    private Integer numberOfTwirls;
    
    // Security validation methods
    @AssertTrue(message = "Request contains invalid data")
    private boolean isSecurityCompliant() {
        return !containsSecurityThreats();
    }
    
    private boolean containsSecurityThreats() {
        // Custom security validation logic
        return hasScriptTags() || hasSqlPatterns() || hasInvalidCharacters();
    }
}

// Validation Error Response DTO
public class ValidationErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private List<FieldError> fieldErrors;
    private String path;
    private String requestId;
    
    public static class FieldError {
        private String field;
        private String message;
        // Non-revealing, standardized messages only
    }
}
```

### Business Logic Guidelines

#### Silliness Assessment Criteria
When implementing silliness assessment logic, consider:
- **Briefcase Integration**: How creatively is the briefcase incorporated? (+10-25 points if has_briefcase=true)
- **Hopping Dynamics**: Quality and rhythm of hopping movements (+15-30 points if involves_hopping=true)
- **Twirl Complexity**: Number and execution of twirls (base: number_of_twirls * 5, bonus for >3 twirls)
- **Description Creativity**: Linguistic creativity and humor in walk description (+10-40 points)
- **Originality Bonus**: Unique combinations of elements (+5-20 points)
- **Safety Deductions**: Risk assessment for public performance (-5 to -15 points for dangerous combinations)

**Scoring Algorithm Example:**
```
base_score = 10
+ (has_briefcase ? 20 : 0)
+ (involves_hopping ? 25 : 0) 
+ (number_of_twirls * 5)
+ description_creativity_score (10-40)
+ combination_bonus (0-20)
- safety_deductions (0-15)
= initial_silliness_score (max 120 points)
```

#### Validation Rules
- **applicant_name**: 2-100 characters, letters and spaces only, no special characters
- **walk_name**: 3-50 characters, alphanumeric and basic punctuation allowed
- **description**: 50-1000 characters, must be descriptive and detailed
- **has_briefcase**: Must be explicit boolean value (true/false)
- **involves_hopping**: Must be explicit boolean value (true/false)
- **number_of_twirls**: Must be non-negative integer between 0 and 100
- Applications must include all required fields (no null values)
- Multiple submissions from same applicant within 30 days require special handling
- Duplicate walk names from same applicant are not permitted

#### Security-First Validation Implementation Requirements

**Validation Processing Order (CRITICAL):**
1. **Content-Type Validation**: Verify JSON content type header
2. **Request Size Validation**: Enforce maximum request body size (e.g., 10KB)
3. **JSON Structure Validation**: Parse and validate JSON format integrity
4. **Field Presence Validation**: Ensure all required fields are present
5. **Data Type Validation**: Strict type checking for each field
6. **Format & Character Set Validation**: Character whitelist enforcement
7. **Length Boundary Validation**: Exact length constraint verification
8. **Business Rule Validation**: Domain-specific validation rules
9. **Cross-Field Validation**: Logical consistency between fields

**Validation Failure Handling:**
- Fail fast: Stop processing on first validation failure
- Log all validation failures with sanitized request data
- Return standardized error responses (no sensitive information)
- Implement rate limiting for repeated validation failures
- Monitor for potential attack patterns in validation failures

**Required Validation Annotations:**
```java
public class ApplicationSubmissionRequest {
    @NotBlank(message = "Applicant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Invalid name format")
    private String applicantName;
    
    @NotBlank(message = "Walk name is required")
    @Size(min = 3, max = 50, message = "Walk name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.!]+$", message = "Invalid walk name format")
    private String walkName;
    
    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 1000, message = "Description must be between 50 and 1000 characters")
    @NoScriptTags // Custom validation to prevent XSS
    @NoSqlKeywords // Custom validation to prevent SQL injection
    private String description;
    
    @NotNull(message = "Briefcase field is required")
    private Boolean hasBriefcase;
    
    @NotNull(message = "Hopping field is required")
    private Boolean involvesHopping;
    
    @NotNull(message = "Number of twirls is required")
    @Min(value = 0, message = "Twirls cannot be negative")
    @Max(value = 100, message = "Twirls cannot exceed 100")
    private Integer numberOfTwirls;
}
```

### Error Handling

#### Standard Error Response Format
```json
{
  "timestamp": "2025-10-31T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request contains invalid data",
  "path": "/api/v1/applications",
  "request_id": "12345-67890"
}
```

**Security-Focused Error Response Guidelines:**
- **Non-Revealing Messages**: Error messages provide guidance without exposing system internals
- **Consistent Format**: All error responses follow identical structure
- **No Stack Traces**: Never expose internal application details
- **Generic Security Messages**: Validation failures use standard messages
- **Request Tracing**: Include request_id for support while maintaining privacy
- **Rate Limiting Headers**: Include rate limit information in error responses

**Error Message Categories:**
1. **Format Errors**: "Invalid request format" (for JSON parsing, type mismatches)
2. **Validation Errors**: "Request contains invalid data" (for constraint violations)
3. **Security Violations**: "Request cannot be processed" (for suspicious patterns)
4. **Rate Limiting**: "Too many requests" (for rate limit exceeded)
5. **Authentication**: "Authentication required" (for missing/invalid auth)

#### Exception Handling Strategy
- **Validation Layer**: Custom validators with security-first design
- **Global Exception Handler**: @ControllerAdvice with standardized error responses
- **Fail-Fast Approach**: Stop processing immediately on validation failure
- **Security Event Logging**: Log all validation failures and suspicious patterns
- **Non-Revealing Responses**: Sanitized error messages that don't expose system details
- **Rate Limiting Integration**: Track and limit repeated validation failures
- **Request Sanitization**: Strip dangerous content before logging

**Required Custom Validators:**
```java
@NoScriptTags        // Prevents XSS via script tag injection
@NoSqlKeywords       // Prevents SQL injection attempts
@ValidCharacterSet   // Enforces character whitelist
@SafeContent         // Comprehensive content safety validation
```

**Security Validation Processing:**
```java
@Component
public class SecurityValidator {
    
    // Validate against XSS patterns
    public boolean containsScriptTags(String input);
    
    // Validate against SQL injection patterns  
    public boolean containsSqlKeywords(String input);
    
    // Validate character set compliance
    public boolean isValidCharacterSet(String input, CharacterSet allowedSet);
    
    // Comprehensive security scan
    public SecurityValidationResult validateContent(String input);
}
```

## Performance & Monitoring

### Performance Considerations
- Database query optimization
- Caching strategy for silliness categories
- Asynchronous processing for heavy assessments
- Connection pooling configuration

### Monitoring & Observability
- Application metrics with Micrometer/Actuator
- Health check endpoints
- Logging strategy with structured logs
- Performance monitoring for critical operations

## Deployment & Configuration

### Environment-Specific Configurations
- Development: H2 in-memory database, detailed logging
- Testing: PostgreSQL test containers, comprehensive test data
- Production: PostgreSQL, optimized logging, security hardening

### Configuration Management
- Externalized configuration via application.yml
- Environment-specific property files
- Secure handling of sensitive configurations
- Docker containerization support

## Communication Style

When generating code or suggestions for this project:

1. **Maintain the humorous theme** while implementing serious backend functionality
2. **Prioritize security** in all recommendations
3. **Suggest comprehensive documentation** for every feature
4. **Include proper error handling** and validation
5. **Follow Spring Boot best practices** consistently
6. **Consider the "Ministry of Silly Walks" context** in naming and comments
7. **Always suggest complete, production-ready implementations**

## Example Code Generation Guidelines

When asked to implement features, always include:
- Complete controller with proper annotations
- Service layer with business logic
- Repository interface if data persistence is needed
- DTO classes for request/response
- Exception handling
- Unit tests for the service layer
- Integration tests for the controller
- OpenAPI documentation annotations

Remember: This system serves the important governmental function of assessing silly walk grants. Treat it with the seriousness it deserves while maintaining the whimsical nature of its purpose!