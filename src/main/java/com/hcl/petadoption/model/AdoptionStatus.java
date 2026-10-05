package com.hcl.petadoption.model;

/**
 * Represents the lifecycle status of an adoption application.
 * Maps directly to the adoption_application table status column (Assessment 2+).
 */
public enum AdoptionStatus {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled");

    private final String displayName;

    AdoptionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
