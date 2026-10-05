package com.hcl.petadoption.service;

import com.hcl.petadoption.exception.AdoptionApplicationException;
import com.hcl.petadoption.exception.DuplicateAdoptionException;
import com.hcl.petadoption.exception.PetNotFoundException;
import com.hcl.petadoption.model.Adopter;
import com.hcl.petadoption.model.AdoptionApplication;
import com.hcl.petadoption.model.AdoptionStatus;
import com.hcl.petadoption.model.HomeVisit;
import com.hcl.petadoption.model.Pet;
import com.hcl.petadoption.model.PetStatus;
import com.hcl.petadoption.repository.AdopterRepository;
import com.hcl.petadoption.repository.AdoptionApplicationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service encapsulating the adoption workflow business rules.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>Checked exception</b>   – {@link AdoptionApplicationException} declared with throws</li>
 *   <li><b>Unchecked exception</b> – {@link DuplicateAdoptionException}, {@link PetNotFoundException}</li>
 *   <li><b>java.time</b>           – {@link LocalDate}, {@link LocalDateTime}</li>
 *   <li><b>Optional</b>            – used to check for existing approved application</li>
 *   <li><b>Business rule</b>       – one approved adoption per pet enforced here</li>
 * </ul>
 */
public class AdoptionService {

    private final AdoptionApplicationRepository applicationRepository;
    private final AdopterRepository adopterRepository;
    private final PetService petService;
    private final HomeVisitService homeVisitService;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public AdoptionService(AdoptionApplicationRepository applicationRepository,
                           AdopterRepository adopterRepository,
                           PetService petService,
                           HomeVisitService homeVisitService) {
        this.applicationRepository = applicationRepository;
        this.adopterRepository     = adopterRepository;
        this.petService            = petService;
        this.homeVisitService      = homeVisitService;
    }

    // -------------------------------------------------------------------------
    // Adopter management
    // -------------------------------------------------------------------------

    /**
     * Registers a new adopter or retrieves one by email if already registered.
     */
    public Adopter registerOrFindAdopter(String name, String email, String phone)
            throws AdoptionApplicationException {
        if (name == null || name.isBlank()) {
            throw new AdoptionApplicationException("APPLICANT_DATA_INVALID",
                    "Applicant name must not be blank.");
        }
        if (email == null || !email.contains("@")) {
            throw new AdoptionApplicationException("APPLICANT_DATA_INVALID",
                    "A valid email address is required.");
        }

        Optional<Adopter> existing = adopterRepository.findByEmail(email);
        if (existing.isPresent()) {
            return existing.get();
        }

        String adopterId = "ADT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Adopter adopter = new Adopter(adopterId, name, email, phone);
        adopterRepository.save(adopter);
        return adopter;
    }

    public Optional<Adopter> findAdopterById(String adopterId) {
        return adopterRepository.findById(adopterId);
    }

    // -------------------------------------------------------------------------
    // Application submission
    // -------------------------------------------------------------------------

    /**
     * Submits a new adoption application.
     *
     * <p>Business rules checked:
     * <ol>
     *   <li>The pet must exist.</li>
     *   <li>The pet must be AVAILABLE.</li>
     * </ol>
     *
     * @param petId     target pet
     * @param adopterId applicant
     * @return the created {@link AdoptionApplication}
     * @throws PetNotFoundException          if the pet does not exist
     * @throws AdoptionApplicationException  if the pet is not available
     */
    public AdoptionApplication submitApplication(String petId, String adopterId)
            throws AdoptionApplicationException {

        Pet pet = petService.findPetOrThrow(petId);   // throws PetNotFoundException (unchecked)

        if (pet.getStatus() != PetStatus.AVAILABLE) {
            throw new AdoptionApplicationException("PET_NOT_AVAILABLE",
                    String.format("Pet '%s' (%s) is not available for adoption. Current status: %s.",
                            pet.getName(), pet.getPetId(), pet.getStatus().getDisplayName()));
        }

        String applicationId = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        AdoptionApplication application = new AdoptionApplication(
                applicationId, petId, adopterId, LocalDate.now());

        applicationRepository.save(application);

        // Mark pet as PENDING once an application is submitted
        petService.updatePetStatus(petId, PetStatus.PENDING);

        return application;
    }

    // -------------------------------------------------------------------------
    // Application review
    // -------------------------------------------------------------------------

    /**
     * Approves an adoption application.
     *
     * <p>Business rules:
     * <ol>
     *   <li>The application must exist and not already be in a terminal state.</li>
     *   <li>No other approved application must exist for the same pet
     *       ({@link DuplicateAdoptionException}).</li>
     *   <li>On approval: pet status → ADOPTED, home visit is scheduled.</li>
     * </ol>
     *
     * @param applicationId the application to approve
     * @param reviewNotes   optional staff notes
     * @return the scheduled {@link HomeVisit}
     * @throws AdoptionApplicationException  if the application is in an invalid state
     * @throws DuplicateAdoptionException    if the pet already has an approved adoption
     */
    public HomeVisit approveApplication(String applicationId, String reviewNotes)
            throws AdoptionApplicationException {

        AdoptionApplication application = findApplicationOrThrow(applicationId);

        if (application.isTerminal()) {
            throw new AdoptionApplicationException("INVALID_STATE",
                    "Application " + applicationId + " is already in a terminal state: "
                    + application.getStatus().getDisplayName());
        }

        // Business rule: one approved adoption per pet
        Optional<AdoptionApplication> existingApproval =
                applicationRepository.findApprovedApplicationForPet(application.getPetId());
        if (existingApproval.isPresent()) {
            throw new DuplicateAdoptionException(
                    application.getPetId(), existingApproval.get().getApplicationId());
        }

        // Approve the application
        application.setStatus(AdoptionStatus.APPROVED);
        application.setReviewNotes(reviewNotes != null ? reviewNotes : "");
        applicationRepository.save(application);

        // Update pet status to ADOPTED
        petService.updatePetStatus(application.getPetId(), PetStatus.ADOPTED);

        // Schedule home visit (default: 7 days from now)
        HomeVisit visit = homeVisitService.scheduleVisit(applicationId,
                LocalDateTime.now().plusDays(7));

        return visit;
    }

    /**
     * Rejects an adoption application.
     *
     * @param applicationId the application to reject
     * @param reviewNotes   reason for rejection
     * @throws AdoptionApplicationException if already in a terminal state
     */
    public void rejectApplication(String applicationId, String reviewNotes)
            throws AdoptionApplicationException {

        AdoptionApplication application = findApplicationOrThrow(applicationId);

        if (application.isTerminal()) {
            throw new AdoptionApplicationException("INVALID_STATE",
                    "Application " + applicationId + " is already in a terminal state: "
                    + application.getStatus().getDisplayName());
        }

        application.setStatus(AdoptionStatus.REJECTED);
        application.setReviewNotes(reviewNotes != null ? reviewNotes : "");
        applicationRepository.save(application);

        // Return pet to AVAILABLE if no other pending applications exist
        List<AdoptionApplication> pendingForPet =
                applicationRepository.findByStatus(AdoptionStatus.SUBMITTED);
        boolean otherPending = pendingForPet.stream()
                .anyMatch(a -> a.getPetId().equals(application.getPetId())
                        && !a.getApplicationId().equals(applicationId));

        if (!otherPending) {
            petService.updatePetStatus(application.getPetId(), PetStatus.AVAILABLE);
        }
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public List<AdoptionApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<AdoptionApplication> getApplicationsForPet(String petId) {
        return applicationRepository.findByPetId(petId);
    }

    public List<AdoptionApplication> getApplicationsByAdopter(String adopterId) {
        return applicationRepository.findByAdopterId(adopterId);
    }

    public Optional<AdoptionApplication> findApplicationById(String applicationId) {
        return applicationRepository.findById(applicationId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private AdoptionApplication findApplicationOrThrow(String applicationId)
            throws AdoptionApplicationException {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AdoptionApplicationException("NOT_FOUND",
                        "No adoption application found with ID: " + applicationId));
    }
}
