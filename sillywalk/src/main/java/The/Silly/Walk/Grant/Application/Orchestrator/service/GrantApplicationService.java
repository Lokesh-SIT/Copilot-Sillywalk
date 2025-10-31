package The.Silly.Walk.Grant.Application.Orchestrator.service;

import The.Silly.Walk.Grant.Application.Orchestrator.dto.ApplicationSubmissionRequest;
import The.Silly.Walk.Grant.Application.Orchestrator.dto.ApplicationSubmissionResponse;
import The.Silly.Walk.Grant.Application.Orchestrator.exception.ApplicationValidationException;
import The.Silly.Walk.Grant.Application.Orchestrator.exception.DuplicateApplicationException;
import The.Silly.Walk.Grant.Application.Orchestrator.exception.SecurityViolationException;
import The.Silly.Walk.Grant.Application.Orchestrator.model.ApplicationStatus;
import The.Silly.Walk.Grant.Application.Orchestrator.model.GrantApplication;
import The.Silly.Walk.Grant.Application.Orchestrator.repository.GrantApplicationRepository;
import The.Silly.Walk.Grant.Application.Orchestrator.validation.SecurityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Core service for processing grant applications to the Ministry of Silly Walks.
 * Implements comprehensive validation, security checks, and silliness assessment
 * as the first line of defense for the Grand Council of Gait.
 */
@Service
@Transactional
public class GrantApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(GrantApplicationService.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    private final GrantApplicationRepository repository;
    private final SecurityValidator securityValidator;

    // Business rule constants as per Ministry regulations
    private static final int DUPLICATE_SUBMISSION_DAYS = 30;
    private static final int MAX_SUBMISSIONS_PER_PERIOD = 3;
    private static final int MIN_SILLINESS_THRESHOLD = 40;

    @Autowired
    public GrantApplicationService(GrantApplicationRepository repository, 
                                 SecurityValidator securityValidator) {
        this.repository = repository;
        this.securityValidator = securityValidator;
    }

    /**
     * Submit a new grant application to the Ministry of Silly Walks.
     * Implements rigorous security validation and silliness assessment.
     * 
     * @param request The application submission request
     * @param requestId Unique request identifier for tracking
     * @return ApplicationSubmissionResponse with application details
     * @throws SecurityViolationException if security threats are detected
     * @throws DuplicateApplicationException if duplicate application is found
     * @throws ApplicationValidationException if business rules are violated
     */
    public ApplicationSubmissionResponse submitApplication(ApplicationSubmissionRequest request, String requestId) {
        logger.info("Processing application submission for applicant: {} with requestId: {}", 
                   sanitizeForLogging(request.getApplicantName()), requestId);

        // Phase 1: Security-First Validation (as per Copilot instructions)
        performSecurityValidation(request, requestId);

        // Phase 2: Business Rule Validation
        performBusinessRuleValidation(request, requestId);

        // Phase 3: Duplicate Detection
        checkForDuplicateApplications(request, requestId);

        // Phase 4: Create and Assess Application
        GrantApplication application = createAndAssessApplication(request);

        // Phase 5: Persist to Database
        GrantApplication savedApplication = repository.save(application);

        logger.info("Successfully submitted application {} for applicant: {} with silliness score: {}", 
                   savedApplication.getApplicationId(), 
                   sanitizeForLogging(savedApplication.getApplicantName()),
                   savedApplication.getInitialSillinessScore());

        return buildSuccessResponse(savedApplication, requestId);
    }

    /**
     * Perform comprehensive security validation as the first line of defense.
     */
    private void performSecurityValidation(ApplicationSubmissionRequest request, String requestId) {
        securityLogger.info("Performing security validation for requestId: {}", requestId);

        // Validate all string fields for security threats
        SecurityValidator.SecurityValidationResult result = securityValidator.validateMultipleFields(
            request.getApplicantName(),
            request.getWalkName(), 
            request.getDescription()
        );

        if (!result.isValid()) {
            securityLogger.warn("Security violation detected - Type: {}, RequestId: {}", 
                              result.getViolationType(), requestId);
            throw new SecurityViolationException("Request contains invalid data", 
                                                result.getViolationType());
        }

        // Additional field-specific validation
        validateFieldCharacterSets(request, requestId);

        securityLogger.info("Security validation passed for requestId: {}", requestId);
    }

    /**
     * Validate character sets for each field according to security requirements.
     */
    private void validateFieldCharacterSets(ApplicationSubmissionRequest request, String requestId) {
        // Applicant name validation
        if (!securityValidator.isValidCharacterSet(request.getApplicantName(), 
                                                  SecurityValidator.CharacterSet.NAME_CHARACTERS)) {
            securityLogger.warn("Invalid character set in applicant name, requestId: {}", requestId);
            throw new SecurityViolationException("Invalid name format", "INVALID_CHARACTERS");
        }

        // Walk name validation
        if (!securityValidator.isValidCharacterSet(request.getWalkName(), 
                                                  SecurityValidator.CharacterSet.WALK_NAME_SAFE)) {
            securityLogger.warn("Invalid character set in walk name, requestId: {}", requestId);
            throw new SecurityViolationException("Invalid walk name format", "INVALID_CHARACTERS");
        }

        // Description validation (using sanitized input for additional safety)
        String sanitizedDescription = securityValidator.sanitizeInput(request.getDescription());
        if (!sanitizedDescription.equals(request.getDescription())) {
            securityLogger.warn("Description required sanitization, potential threat detected, requestId: {}", requestId);
            throw new SecurityViolationException("Invalid description content", "CONTENT_SANITIZATION_REQUIRED");
        }
    }

    /**
     * Perform business rule validation according to Ministry standards.
     */
    private void performBusinessRuleValidation(ApplicationSubmissionRequest request, String requestId) {
        logger.debug("Performing business rule validation for requestId: {}", requestId);

        // Validate twirl limits (reasonable bounds for safety)
        if (request.getNumberOfTwirls() > 50) {
            logger.warn("Excessive twirl count detected: {} for requestId: {}", 
                       request.getNumberOfTwirls(), requestId);
            // Don't throw exception but log for review - Ministry allows creative expression
        }

        // Validate description quality (more detailed than basic length check)
        if (!isDescriptionSufficientlyDetailed(request.getDescription())) {
            throw new ApplicationValidationException("Description lacks sufficient detail for Ministry review");
        }

        // Check for submission frequency limits
        checkSubmissionFrequency(request.getApplicantName(), requestId);

        logger.debug("Business rule validation passed for requestId: {}", requestId);
    }

    /**
     * Check if description provides sufficient detail for silliness assessment.
     */
    private boolean isDescriptionSufficientlyDetailed(String description) {
        if (description == null || description.trim().length() < 50) {
            return false;
        }

        // Check for meaningful content (not just repeated characters)
        String[] words = description.split("\\s+");
        if (words.length < 10) {
            return false; // Too few words for proper assessment
        }

        // Check for silly walk related terminology
        String lowerDesc = description.toLowerCase();
        boolean hasWalkTerminology = lowerDesc.contains("walk") || lowerDesc.contains("step") || 
                                    lowerDesc.contains("gait") || lowerDesc.contains("movement");
        
        return hasWalkTerminology; // Must reference walking/movement concepts
    }

    /**
     * Check submission frequency to prevent spam (30-day rule).
     */
    private void checkSubmissionFrequency(String applicantName, String requestId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(DUPLICATE_SUBMISSION_DAYS);
        long recentSubmissions = repository.countRecentSubmissionsByApplicant(applicantName, thirtyDaysAgo);

        if (recentSubmissions >= MAX_SUBMISSIONS_PER_PERIOD) {
            logger.warn("Submission frequency limit exceeded for applicant: {} ({}), requestId: {}", 
                       sanitizeForLogging(applicantName), recentSubmissions, requestId);
            throw new ApplicationValidationException("Maximum submissions exceeded for this period");
        }
    }

    /**
     * Check for duplicate applications (same applicant + walk name).
     */
    private void checkForDuplicateApplications(ApplicationSubmissionRequest request, String requestId) {
        Optional<GrantApplication> existing = repository.findByApplicantNameAndWalkNameIgnoreCase(
            request.getApplicantName(), request.getWalkName());

        if (existing.isPresent()) {
            logger.warn("Duplicate application detected for applicant: {} and walk: {}, requestId: {}", 
                       sanitizeForLogging(request.getApplicantName()), 
                       sanitizeForLogging(request.getWalkName()), requestId);
            throw new DuplicateApplicationException("An application for this walk already exists");
        }
    }

    /**
     * Create new application and perform silliness assessment.
     */
    private GrantApplication createAndAssessApplication(ApplicationSubmissionRequest request) {
        GrantApplication application = new GrantApplication(
            request.getApplicantName(),
            request.getWalkName(),
            request.getDescription(),
            request.getHasBriefcase(),
            request.getInvolvesHopping(),
            request.getNumberOfTwirls()
        );

        // Calculate initial silliness score using the entity's algorithm
        int sillinessScore = application.calculateSillinessScore();
        application.setInitialSillinessScore(sillinessScore);

        // Set appropriate status based on score
        if (sillinessScore >= MIN_SILLINESS_THRESHOLD) {
            application.setStatus(ApplicationStatus.SUBMITTED);
        } else {
            application.setStatus(ApplicationStatus.PENDING_INFO);
            logger.info("Application requires additional review due to low silliness score: {}", sillinessScore);
        }

        return application;
    }

    /**
     * Build successful response DTO.
     */
    private ApplicationSubmissionResponse buildSuccessResponse(GrantApplication application, String requestId) {
        return ApplicationSubmissionResponse.builder()
            .applicationId(application.getApplicationId())
            .status(application.getStatus().name().toLowerCase())
            .submittedAt(application.getSubmittedAt())
            .initialSillinessScore(application.getInitialSillinessScore())
            .message("Application successfully submitted for preliminary review")
            .requestId(requestId)
            .build();
    }

    /**
     * Retrieve application by ID with security checks.
     */
    @Transactional(readOnly = true)
    public Optional<GrantApplication> getApplicationById(UUID applicationId) {
        return repository.findById(applicationId);
    }

    /**
     * Generate application statistics for Ministry dashboard.
     */
    @Transactional(readOnly = true)
    public GrantApplicationRepository.ApplicationStatistics getApplicationStatistics(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return repository.getApplicationStatistics(since);
    }

    /**
     * Sanitize strings for logging to prevent log injection.
     */
    private String sanitizeForLogging(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_")
                   .replaceAll("[\\p{Cntrl}]", "?")
                   .substring(0, Math.min(input.length(), 50));
    }

    /**
     * Generate unique request ID for tracking.
     */
    public String generateRequestId() {
        return "req_" + UUID.randomUUID().toString().substring(0, 8) + "_" + 
               System.currentTimeMillis();
    }
}