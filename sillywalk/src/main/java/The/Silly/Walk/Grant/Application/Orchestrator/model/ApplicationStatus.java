package The.Silly.Walk.Grant.Application.Orchestrator.model;

/**
 * Application status enum for tracking the lifecycle of grant applications
 * submitted to the Ministry of Silly Walks.
 */
public enum ApplicationStatus {
    /**
     * Application has been submitted and is awaiting initial review
     */
    SUBMITTED("Application submitted and awaiting initial assessment"),
    
    /**
     * Application is currently under review by the Ministry's assessment team
     */
    UNDER_REVIEW("Application is being reviewed for silliness compliance"),
    
    /**
     * Application has been approved for further consideration by the Grand Council of Gait
     */
    APPROVED("Application approved - proceeding to Grand Council review"),
    
    /**
     * Application has been rejected due to insufficient silliness or other criteria
     */
    REJECTED("Application rejected - does not meet Ministry standards"),
    
    /**
     * Application requires additional information or clarification
     */
    PENDING_INFO("Application pending - additional information required"),
    
    /**
     * Application has been withdrawn by the applicant
     */
    WITHDRAWN("Application withdrawn by applicant");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if the status represents a final state (no further processing)
     */
    public boolean isFinalStatus() {
        return this == APPROVED || this == REJECTED || this == WITHDRAWN;
    }

    /**
     * Check if the status allows for updates to the application
     */
    public boolean allowsUpdates() {
        return this == SUBMITTED || this == PENDING_INFO;
    }
}