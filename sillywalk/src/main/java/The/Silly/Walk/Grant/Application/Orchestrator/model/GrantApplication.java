package The.Silly.Walk.Grant.Application.Orchestrator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Grant Application entity representing a submission to the Ministry of Silly Walks.
 * Only the silliest (and most securely processed) walks receive due consideration.
 */
@Entity
@Table(name = "grant_applications",
       uniqueConstraints = @UniqueConstraint(columnNames = {"applicant_name", "walk_name"}))
public class GrantApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "application_id")
    private UUID applicationId;

    @NotBlank(message = "Applicant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Invalid name format")
    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;

    @NotBlank(message = "Walk name is required")
    @Size(min = 3, max = 50, message = "Walk name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.!]+$", message = "Invalid walk name format")
    @Column(name = "walk_name", nullable = false, length = 50)
    private String walkName;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 1000, message = "Description must be between 50 and 1000 characters")
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Briefcase field is required")
    @Column(name = "has_briefcase", nullable = false)
    private Boolean hasBriefcase;

    @NotNull(message = "Hopping field is required")
    @Column(name = "involves_hopping", nullable = false)
    private Boolean involvesHopping;

    @NotNull(message = "Number of twirls is required")
    @Min(value = 0, message = "Twirls cannot be negative")
    @Max(value = 100, message = "Twirls cannot exceed 100")
    @Column(name = "number_of_twirls", nullable = false)
    private Integer numberOfTwirls;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Min(value = 0, message = "Silliness score cannot be negative")
    @Max(value = 120, message = "Silliness score cannot exceed 120")
    @Column(name = "initial_silliness_score")
    private Integer initialSillinessScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public GrantApplication() {
        this.status = ApplicationStatus.SUBMITTED;
        this.initialSillinessScore = 0;
    }

    // Constructor for new applications
    public GrantApplication(String applicantName, String walkName, String description,
                           Boolean hasBriefcase, Boolean involvesHopping, Integer numberOfTwirls) {
        this();
        this.applicantName = applicantName;
        this.walkName = walkName;
        this.description = description;
        this.hasBriefcase = hasBriefcase;
        this.involvesHopping = involvesHopping;
        this.numberOfTwirls = numberOfTwirls;
        this.submittedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getWalkName() {
        return walkName;
    }

    public void setWalkName(String walkName) {
        this.walkName = walkName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getHasBriefcase() {
        return hasBriefcase;
    }

    public void setHasBriefcase(Boolean hasBriefcase) {
        this.hasBriefcase = hasBriefcase;
    }

    public Boolean getInvolvesHopping() {
        return involvesHopping;
    }

    public void setInvolvesHopping(Boolean involvesHopping) {
        this.involvesHopping = involvesHopping;
    }

    public Integer getNumberOfTwirls() {
        return numberOfTwirls;
    }

    public void setNumberOfTwirls(Integer numberOfTwirls) {
        this.numberOfTwirls = numberOfTwirls;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Integer getInitialSillinessScore() {
        return initialSillinessScore;
    }

    public void setInitialSillinessScore(Integer initialSillinessScore) {
        this.initialSillinessScore = initialSillinessScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Calculate the initial silliness score based on application characteristics.
     * This implements the scoring algorithm specified in the Copilot instructions.
     * 
     * @return calculated silliness score (0-120 points)
     */
    public int calculateSillinessScore() {
        int score = 10; // Base score for any application to the Ministry

        // Briefcase integration bonus (as per Copilot instructions)
        if (Boolean.TRUE.equals(hasBriefcase)) {
            score += 20;
        }

        // Hopping dynamics assessment  
        if (Boolean.TRUE.equals(involvesHopping)) {
            score += 25;
        }

        // Twirl complexity calculation
        score += numberOfTwirls * 5;

        // Bonus for exceptional twirl counts
        if (numberOfTwirls > 3) {
            score += 10; // Combination bonus for complex twirling
        }

        // Description creativity score (simplified assessment)
        score += assessDescriptionCreativity();

        // Safety deductions for potentially dangerous combinations
        score -= assessSafetyRisks();

        // Ensure score stays within valid range
        return Math.max(0, Math.min(120, score));
    }

    /**
     * Assess the creativity of the walk description.
     * Higher scores for longer, more descriptive text.
     */
    private int assessDescriptionCreativity() {
        if (description == null) return 0;
        
        int creativityScore = 10; // Base creativity score
        
        // Length bonus
        if (description.length() > 200) creativityScore += 10;
        if (description.length() > 500) creativityScore += 10;
        
        // Keyword bonus for silly walk terminology
        String lowerDesc = description.toLowerCase();
        if (lowerDesc.contains("silly") || lowerDesc.contains("absurd")) creativityScore += 5;
        if (lowerDesc.contains("ridiculous") || lowerDesc.contains("preposterous")) creativityScore += 5;
        if (lowerDesc.contains("ministry") || lowerDesc.contains("pythonesque")) creativityScore += 10;
        
        return Math.min(40, creativityScore); // Cap at 40 points as per instructions
    }

    /**
     * Assess safety risks and apply deductions.
     * Combinations of briefcase + high twirls + hopping may be unsafe.
     */
    private int assessSafetyRisks() {
        int riskScore = 0;
        
        // High-risk combination: briefcase + excessive twirls + hopping
        if (Boolean.TRUE.equals(hasBriefcase) && 
            Boolean.TRUE.equals(involvesHopping) && 
            numberOfTwirls > 10) {
            riskScore += 15; // Maximum safety deduction
        } else if (numberOfTwirls > 20) {
            riskScore += 10; // High twirl count risk
        } else if (Boolean.TRUE.equals(hasBriefcase) && numberOfTwirls > 15) {
            riskScore += 5; // Moderate briefcase + twirl risk
        }
        
        return Math.min(15, riskScore); // Cap at 15 points deduction
    }

    @Override
    public String toString() {
        return "GrantApplication{" +
                "applicationId=" + applicationId +
                ", applicantName='" + applicantName + '\'' +
                ", walkName='" + walkName + '\'' +
                ", status=" + status +
                ", initialSillinessScore=" + initialSillinessScore +
                ", submittedAt=" + submittedAt +
                '}';
    }
}