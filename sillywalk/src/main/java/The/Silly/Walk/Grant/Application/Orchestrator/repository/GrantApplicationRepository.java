package The.Silly.Walk.Grant.Application.Orchestrator.repository;

import The.Silly.Walk.Grant.Application.Orchestrator.model.ApplicationStatus;
import The.Silly.Walk.Grant.Application.Orchestrator.model.GrantApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Grant Applications in the Ministry of Silly Walks system.
 * Provides secure data access with custom queries for silliness assessment and compliance tracking.
 */
@Repository
public interface GrantApplicationRepository extends JpaRepository<GrantApplication, UUID> {

    /**
     * Find applications by applicant name (case-insensitive).
     * Used for duplicate detection and applicant history tracking.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE LOWER(ga.applicantName) = LOWER(:applicantName)")
    List<GrantApplication> findByApplicantNameIgnoreCase(@Param("applicantName") String applicantName);

    /**
     * Find applications by status with pagination support.
     * Essential for Ministry workflow management.
     */
    Page<GrantApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * Find applications by applicant name and walk name to prevent duplicates.
     * Enforces the business rule of unique walk names per applicant.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE LOWER(ga.applicantName) = LOWER(:applicantName) " +
           "AND LOWER(ga.walkName) = LOWER(:walkName)")
    Optional<GrantApplication> findByApplicantNameAndWalkNameIgnoreCase(
        @Param("applicantName") String applicantName, 
        @Param("walkName") String walkName);

    /**
     * Check for recent submissions by the same applicant within specified days.
     * Prevents spam submissions and enforces the 30-day rule.
     */
    @Query("SELECT COUNT(ga) FROM GrantApplication ga WHERE LOWER(ga.applicantName) = LOWER(:applicantName) " +
           "AND ga.submittedAt >= :since")
    long countRecentSubmissionsByApplicant(
        @Param("applicantName") String applicantName, 
        @Param("since") LocalDateTime since);

    /**
     * Find applications with silliness score above threshold.
     * Used by the Grand Council for identifying the silliest walks.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE ga.initialSillinessScore >= :minScore " +
           "ORDER BY ga.initialSillinessScore DESC")
    List<GrantApplication> findBySillinessScoreGreaterThanEqual(@Param("minScore") Integer minScore);

    /**
     * Find applications submitted within a date range.
     * Supports reporting and performance analytics.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE ga.submittedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ga.submittedAt DESC")
    List<GrantApplication> findBySubmittedAtBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find applications with specific characteristics for analysis.
     * Supports silliness pattern analysis and Ministry research.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE " +
           "(:hasBriefcase IS NULL OR ga.hasBriefcase = :hasBriefcase) AND " +
           "(:involvesHopping IS NULL OR ga.involvesHopping = :involvesHopping) AND " +
           "(:minTwirls IS NULL OR ga.numberOfTwirls >= :minTwirls) AND " +
           "(:maxTwirls IS NULL OR ga.numberOfTwirls <= :maxTwirls)")
    List<GrantApplication> findByCharacteristics(
        @Param("hasBriefcase") Boolean hasBriefcase,
        @Param("involvesHopping") Boolean involvesHopping,
        @Param("minTwirls") Integer minTwirls,
        @Param("maxTwirls") Integer maxTwirls);

    /**
     * Find top silly walks for Grand Council consideration.
     * Returns the highest scoring applications for review.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE ga.status = :status " +
           "ORDER BY ga.initialSillinessScore DESC")
    List<GrantApplication> findTopApplicationsByScore(
        @Param("status") ApplicationStatus status, 
        Pageable pageable);

    /**
     * Get application statistics for Ministry dashboard.
     * Provides aggregated data for reporting purposes.
     */
    @Query("SELECT " +
           "COUNT(ga) as totalApplications, " +
           "AVG(ga.initialSillinessScore) as averageScore, " +
           "MAX(ga.initialSillinessScore) as highestScore, " +
           "COUNT(CASE WHEN ga.hasBriefcase = true THEN 1 END) as briefcaseWalks, " +
           "COUNT(CASE WHEN ga.involvesHopping = true THEN 1 END) as hoppingWalks " +
           "FROM GrantApplication ga WHERE ga.submittedAt >= :since")
    ApplicationStatistics getApplicationStatistics(@Param("since") LocalDateTime since);

    /**
     * Interface for application statistics projection.
     */
    interface ApplicationStatistics {
        Long getTotalApplications();
        Double getAverageScore();
        Integer getHighestScore();
        Long getBriefcaseWalks();
        Long getHoppingWalks();
    }

    /**
     * Find applications that need security review.
     * Used for identifying applications with unusual patterns.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE " +
           "ga.description LIKE '%' || :suspiciousPattern || '%' " +
           "OR LENGTH(ga.description) > :maxDescriptionLength " +
           "OR ga.numberOfTwirls > :maxTwirls")
    List<GrantApplication> findSuspiciousApplications(
        @Param("suspiciousPattern") String suspiciousPattern,
        @Param("maxDescriptionLength") Integer maxDescriptionLength,
        @Param("maxTwirls") Integer maxTwirls);

    /**
     * Find applications by walk name pattern for similarity detection.
     * Helps identify potentially duplicate or similar walk concepts.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE LOWER(ga.walkName) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<GrantApplication> findByWalkNameContainingIgnoreCase(@Param("pattern") String pattern);

    /**
     * Update application status (for batch operations).
     * Used by Ministry staff for bulk status updates.
     */
    @Query("UPDATE GrantApplication ga SET ga.status = :newStatus, ga.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ga.applicationId IN :applicationIds AND ga.status = :currentStatus")
    int updateApplicationStatus(
        @Param("applicationIds") List<UUID> applicationIds,
        @Param("currentStatus") ApplicationStatus currentStatus,
        @Param("newStatus") ApplicationStatus newStatus);

    /**
     * Delete applications older than specified date (for data retention).
     * Supports Ministry data retention policies.
     */
    @Query("DELETE FROM GrantApplication ga WHERE ga.submittedAt < :cutoffDate " +
           "AND ga.status IN (:eligibleStatuses)")
    int deleteOldApplications(
        @Param("cutoffDate") LocalDateTime cutoffDate,
        @Param("eligibleStatuses") List<ApplicationStatus> eligibleStatuses);

    /**
     * Check if applicant exists in the system.
     * Quick existence check for validation purposes.
     */
    boolean existsByApplicantNameIgnoreCase(String applicantName);

    /**
     * Count applications by status for dashboard widgets.
     */
    long countByStatus(ApplicationStatus status);

    /**
     * Find the most recent application by applicant for duplicate detection.
     */
    @Query("SELECT ga FROM GrantApplication ga WHERE LOWER(ga.applicantName) = LOWER(:applicantName) " +
           "ORDER BY ga.submittedAt DESC")
    Optional<GrantApplication> findMostRecentByApplicant(@Param("applicantName") String applicantName);
}