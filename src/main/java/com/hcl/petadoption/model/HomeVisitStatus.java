package com.hcl.petadoption.model;

/**
 * Lifecycle status of a scheduled home visit.
 */
public enum HomeVisitStatus {
    SCHEDULED("Scheduled"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    HomeVisitStatus(String displayName) {
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
