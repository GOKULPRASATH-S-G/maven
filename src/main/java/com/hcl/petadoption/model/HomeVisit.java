package com.hcl.petadoption.model;

import java.time.LocalDateTime;

/**
 * Represents a home-visit appointment scheduled after an adoption is approved.
 *
 * <p>Home visits are maintained in a {@link java.util.Queue} in the service layer,
 * demonstrating FIFO scheduling behaviour.
 *
 * <p>Key relationships (future DB columns):
 * <ul>
 *   <li>{@code applicationId} – FK → {@code adoption_application.application_id}</li>
 * </ul>
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Encapsulation</b> – private fields with getters/setters</li>
 *   <li><b>java.time</b>     – {@code scheduledDateTime} uses {@link LocalDateTime}</li>
 * </ul>
 */
public class HomeVisit {

    private final String visitId;
    private final String applicationId;
    private LocalDateTime scheduledDateTime;
    private HomeVisitStatus status;
    private String staffNotes;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new HomeVisit in SCHEDULED state.
     *
     * @param visitId           unique visit identifier
     * @param applicationId     ID of the approved adoption application
     * @param scheduledDateTime date and time the visit is scheduled
     */
    public HomeVisit(String visitId, String applicationId, LocalDateTime scheduledDateTime) {
        if (visitId == null || visitId.isBlank()) {
            throw new IllegalArgumentException("Visit ID must not be blank.");
        }
        if (applicationId == null || applicationId.isBlank()) {
            throw new IllegalArgumentException("Application ID reference must not be blank.");
        }
        this.visitId           = visitId;
        this.applicationId     = applicationId;
        this.scheduledDateTime = scheduledDateTime != null
                ? scheduledDateTime
                : LocalDateTime.now().plusDays(7);
        this.status            = HomeVisitStatus.SCHEDULED;
        this.staffNotes        = "";
    }

    // -------------------------------------------------------------------------
    // Accessors / Mutators
    // -------------------------------------------------------------------------

    public String getVisitId() { return visitId; }

    public String getApplicationId() { return applicationId; }

    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }

    public void setScheduledDateTime(LocalDateTime scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }

    public HomeVisitStatus getStatus() { return status; }

    public void setStatus(HomeVisitStatus status) {
        this.status = status;
    }

    public String getStaffNotes() { return staffNotes; }

    public void setStaffNotes(String staffNotes) {
        this.staffNotes = staffNotes != null ? staffNotes : "";
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    public String getSummary() {
        return String.format(
                "  Visit ID       : %s%n" +
                "  Application ID : %s%n" +
                "  Scheduled      : %s%n" +
                "  Status         : %s%n" +
                "  Staff Notes    : %s",
                visitId, applicationId,
                scheduledDateTime, status.getDisplayName(),
                staffNotes.isBlank() ? "—" : staffNotes);
    }

    @Override
    public String toString() {
        return String.format("[%s] App:%s – %s – %s",
                visitId, applicationId, scheduledDateTime, status.getDisplayName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof HomeVisit other)) return false;
        return visitId.equals(other.visitId);
    }

    @Override
    public int hashCode() {
        return visitId.hashCode();
    }
}
