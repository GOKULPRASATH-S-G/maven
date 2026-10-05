package com.hcl.petadoption.model;

import java.time.LocalDate;

/**
 * Represents an adoption application submitted by an {@link Adopter} for a specific {@link Pet}.
 *
 * <p>Key relationships (future DB columns):
 * <ul>
 *   <li>{@code petId}     – FK → {@code pet.pet_id}</li>
 *   <li>{@code adopterId} – FK → {@code adopter.adopter_id}</li>
 * </ul>
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Encapsulation</b>  – private fields with controlled mutators</li>
 *   <li><b>java.time</b>      – {@code applicationDate} uses {@link LocalDate}</li>
 * </ul>
 */
public class AdoptionApplication {

    private final String applicationId;
    private final String petId;
    private final String adopterId;
    private final LocalDate applicationDate;
    private AdoptionStatus status;
    private String reviewNotes;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new AdoptionApplication in SUBMITTED state.
     *
     * @param applicationId unique application ID
     * @param petId         ID of the target pet
     * @param adopterId     ID of the applicant
     * @param applicationDate date the application was submitted
     */
    public AdoptionApplication(String applicationId, String petId,
                               String adopterId, LocalDate applicationDate) {
        if (applicationId == null || applicationId.isBlank()) {
            throw new IllegalArgumentException("Application ID must not be blank.");
        }
        if (petId == null || petId.isBlank()) {
            throw new IllegalArgumentException("Pet ID reference must not be blank.");
        }
        if (adopterId == null || adopterId.isBlank()) {
            throw new IllegalArgumentException("Adopter ID reference must not be blank.");
        }
        this.applicationId   = applicationId;
        this.petId           = petId;
        this.adopterId       = adopterId;
        this.applicationDate = applicationDate != null ? applicationDate : LocalDate.now();
        this.status          = AdoptionStatus.SUBMITTED;
        this.reviewNotes     = "";
    }

    // -------------------------------------------------------------------------
    // Domain helpers
    // -------------------------------------------------------------------------

    /** Returns true if this application is in a terminal (non-changeable) state. */
    public boolean isTerminal() {
        return status == AdoptionStatus.APPROVED
                || status == AdoptionStatus.REJECTED
                || status == AdoptionStatus.CANCELLED;
    }

    /** Returns true if this application has been approved. */
    public boolean isApproved() {
        return status == AdoptionStatus.APPROVED;
    }

    // -------------------------------------------------------------------------
    // Accessors / Mutators
    // -------------------------------------------------------------------------

    public String getApplicationId() { return applicationId; }

    public String getPetId() { return petId; }

    public String getAdopterId() { return adopterId; }

    public LocalDate getApplicationDate() { return applicationDate; }

    public AdoptionStatus getStatus() { return status; }

    public void setStatus(AdoptionStatus status) {
        this.status = status;
    }

    public String getReviewNotes() { return reviewNotes; }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes != null ? reviewNotes : "";
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    public String getSummary() {
        return String.format(
                "  Application ID  : %s%n" +
                "  Pet ID          : %s%n" +
                "  Adopter ID      : %s%n" +
                "  Date            : %s%n" +
                "  Status          : %s%n" +
                "  Review Notes    : %s",
                applicationId, petId, adopterId,
                applicationDate, status.getDisplayName(),
                reviewNotes.isBlank() ? "—" : reviewNotes);
    }

    @Override
    public String toString() {
        return String.format("[%s] Pet:%s Adopter:%s – %s",
                applicationId, petId, adopterId, status.getDisplayName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AdoptionApplication other)) return false;
        return applicationId.equals(other.applicationId);
    }

    @Override
    public int hashCode() {
        return applicationId.hashCode();
    }
}
