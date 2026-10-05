package com.hcl.petadoption;

import com.hcl.petadoption.exception.AdoptionApplicationException;
import com.hcl.petadoption.exception.DuplicateAdoptionException;
import com.hcl.petadoption.exception.PetNotFoundException;
import com.hcl.petadoption.model.Adopter;
import com.hcl.petadoption.model.AdoptionApplication;
import com.hcl.petadoption.model.AdoptionStatus;
import com.hcl.petadoption.model.Dog;
import com.hcl.petadoption.model.Gender;
import com.hcl.petadoption.model.HomeVisit;
import com.hcl.petadoption.model.Pet;
import com.hcl.petadoption.model.PetStatus;
import com.hcl.petadoption.repository.AdopterRepository;
import com.hcl.petadoption.repository.AdoptionApplicationRepository;
import com.hcl.petadoption.repository.PetRepository;
import com.hcl.petadoption.service.AdoptionService;
import com.hcl.petadoption.service.HomeVisitService;
import com.hcl.petadoption.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdoptionService} and {@link HomeVisitService}.
 *
 * <p>Tests:
 * <ol>
 *   <li>Creating an adoption application</li>
 *   <li>Approving an application</li>
 *   <li>Pet status changes to ADOPTED after approval</li>
 *   <li>Preventing duplicate approved adoptions (DuplicateAdoptionException)</li>
 *   <li>Rejecting an application</li>
 *   <li>Pet status reverts to AVAILABLE after rejection</li>
 *   <li>Home-visit Queue enqueue behaviour</li>
 *   <li>Home-visit Queue peek (non-destructive)</li>
 *   <li>Home-visit Queue dequeue (processNextVisit)</li>
 *   <li>Submitting application for unavailable pet throws AdoptionApplicationException</li>
 * </ol>
 */
@DisplayName("AdoptionService and HomeVisitService Tests")
class AdoptionServiceTest {

    private PetService                     petService;
    private AdoptionService                adoptionService;
    private HomeVisitService               homeVisitService;
    private AdopterRepository              adopterRepository;
    private AdoptionApplicationRepository  appRepository;

    private static final String ADOPTER_ID = "ADT-TEST1";
    private static final String PET_ID     = "D-001";

    @BeforeEach
    void setUp() throws AdoptionApplicationException {
        PetRepository petRepo = new PetRepository();
        appRepository         = new AdoptionApplicationRepository();
        adopterRepository     = new AdopterRepository();
        petService            = new PetService(petRepo);
        homeVisitService      = new HomeVisitService();
        adoptionService       = new AdoptionService(appRepository, adopterRepository, petService, homeVisitService);

        // Register a test pet
        petService.registerPet(new Dog(PET_ID, "Max", 3, "Labrador", Gender.MALE, PetStatus.AVAILABLE));

        // Register a test adopter
        adopterRepository.save(new Adopter(ADOPTER_ID, "Test User", "test@example.com", "1234567890"));
    }

    // =========================================================================
    // 7. Creating an adoption application
    // =========================================================================

    @Test
    @DisplayName("Should submit a new adoption application successfully")
    void testSubmitApplication() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);

        assertNotNull(app);
        assertNotNull(app.getApplicationId());
        assertEquals(PET_ID,     app.getPetId());
        assertEquals(ADOPTER_ID, app.getAdopterId());
        assertEquals(AdoptionStatus.SUBMITTED, app.getStatus());
    }

    @Test
    @DisplayName("Pet status should change to PENDING after application is submitted")
    void testPetStatusPendingAfterApplication() throws AdoptionApplicationException {
        adoptionService.submitApplication(PET_ID, ADOPTER_ID);

        Pet pet = petService.findPetOrThrow(PET_ID);
        assertEquals(PetStatus.PENDING, pet.getStatus());
    }

    @Test
    @DisplayName("Should throw PetNotFoundException for unknown pet ID")
    void testSubmitForUnknownPet() {
        assertThrows(PetNotFoundException.class,
                () -> adoptionService.submitApplication("GHOST", ADOPTER_ID));
    }

    @Test
    @DisplayName("Should throw AdoptionApplicationException when pet is not AVAILABLE")
    void testSubmitForUnavailablePet() throws AdoptionApplicationException {
        // Submit once – pet becomes PENDING
        adoptionService.submitApplication(PET_ID, ADOPTER_ID);

        // Submit again for another adopter – should fail
        adopterRepository.save(new Adopter("ADT-TEST2", "Other User", "other@example.com", "0987654321"));
        AdoptionApplicationException ex = assertThrows(AdoptionApplicationException.class,
                () -> adoptionService.submitApplication(PET_ID, "ADT-TEST2"));
        assertEquals("PET_NOT_AVAILABLE", ex.getCode());
    }

    // =========================================================================
    // 8. Approving an application
    // =========================================================================

    @Test
    @DisplayName("Approving an application should set its status to APPROVED")
    void testApproveApplication() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        adoptionService.approveApplication(app.getApplicationId(), "Good candidate");

        AdoptionApplication updated = adoptionService
                .findApplicationById(app.getApplicationId()).orElseThrow();
        assertEquals(AdoptionStatus.APPROVED, updated.getStatus());
        assertEquals("Good candidate", updated.getReviewNotes());
    }

    // =========================================================================
    // 9. Pet status changes to ADOPTED after approval
    // =========================================================================

    @Test
    @DisplayName("Pet status should change to ADOPTED after application approval")
    void testPetStatusAdoptedAfterApproval() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        adoptionService.approveApplication(app.getApplicationId(), null);

        Pet pet = petService.findPetOrThrow(PET_ID);
        assertEquals(PetStatus.ADOPTED, pet.getStatus());
    }

    // =========================================================================
    // 10. Preventing duplicate approved adoption
    // =========================================================================

    @Test
    @DisplayName("Approving a second application for same pet should throw DuplicateAdoptionException")
    void testDuplicateAdoptionThrows() throws AdoptionApplicationException {
        // First application submitted
        AdoptionApplication app1 = adoptionService.submitApplication(PET_ID, ADOPTER_ID);

        // Second application for the same pet submitted (e.g. concurrent or direct entry)
        adopterRepository.save(new Adopter("ADT-TEST2", "Second Applicant", "second@example.com", "9999999999"));
        AdoptionApplication app2 = new AdoptionApplication(
                "APP-CONCURRENT", PET_ID, "ADT-TEST2", java.time.LocalDate.now());
        appRepository.save(app2);

        // Approve first application successfully
        adoptionService.approveApplication(app1.getApplicationId(), "Approved first");

        // Attempting to approve second application for the same pet must throw DuplicateAdoptionException
        DuplicateAdoptionException ex = assertThrows(DuplicateAdoptionException.class,
                () -> adoptionService.approveApplication(app2.getApplicationId(), "Try second"));
        assertEquals(PET_ID, ex.getPetId());
        assertEquals(app1.getApplicationId(), ex.getExistingApplicationId());
    }

    @Test
    @DisplayName("DuplicateAdoptionException message should contain pet ID")
    void testDuplicateAdoptionExceptionDetails() {
        // Manually trigger DuplicateAdoptionException
        DuplicateAdoptionException ex = new DuplicateAdoptionException("D-001", "APP-001");
        assertTrue(ex.getMessage().contains("D-001"));
        assertTrue(ex.getMessage().contains("APP-001"));
        assertEquals("D-001",   ex.getPetId());
        assertEquals("APP-001", ex.getExistingApplicationId());
    }

    // =========================================================================
    // Rejecting an application
    // =========================================================================

    @Test
    @DisplayName("Rejecting an application should set its status to REJECTED")
    void testRejectApplication() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        adoptionService.rejectApplication(app.getApplicationId(), "Insufficient space");

        AdoptionApplication updated = adoptionService
                .findApplicationById(app.getApplicationId()).orElseThrow();
        assertEquals(AdoptionStatus.REJECTED, updated.getStatus());
    }

    @Test
    @DisplayName("Pet should revert to AVAILABLE after rejection (no other pending apps)")
    void testPetAvailableAfterRejection() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        adoptionService.rejectApplication(app.getApplicationId(), "Not suitable");

        Pet pet = petService.findPetOrThrow(PET_ID);
        assertEquals(PetStatus.AVAILABLE, pet.getStatus());
    }

    @Test
    @DisplayName("Cannot approve a terminal (already rejected) application")
    void testCannotApproveTerminalApplication() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        adoptionService.rejectApplication(app.getApplicationId(), "");

        AdoptionApplicationException ex = assertThrows(AdoptionApplicationException.class,
                () -> adoptionService.approveApplication(app.getApplicationId(), ""));
        assertEquals("INVALID_STATE", ex.getCode());
    }

    // =========================================================================
    // 11. Home-visit Queue behaviour
    // =========================================================================

    @Test
    @DisplayName("Queue should be empty before any approvals")
    void testQueueEmptyInitially() {
        assertEquals(0, homeVisitService.pendingCount());
        assertTrue(homeVisitService.peekNextVisit().isEmpty());
    }

    @Test
    @DisplayName("Approving an application should enqueue a home visit")
    void testHomeVisitEnqueuedOnApproval() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        HomeVisit visit = adoptionService.approveApplication(app.getApplicationId(), "");

        assertNotNull(visit);
        assertEquals(1, homeVisitService.pendingCount());
        assertEquals(app.getApplicationId(), visit.getApplicationId());
    }

    @Test
    @DisplayName("peekNextVisit should return the scheduled visit without removing it")
    void testQueuePeek() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        HomeVisit scheduled = adoptionService.approveApplication(app.getApplicationId(), "");

        HomeVisit peeked = homeVisitService.peekNextVisit().orElseThrow();
        assertEquals(scheduled.getVisitId(), peeked.getVisitId());
        // Peek must NOT remove the element
        assertEquals(1, homeVisitService.pendingCount());
    }

    @Test
    @DisplayName("processNextVisit should dequeue and mark visit COMPLETED")
    void testQueueDequeue() throws AdoptionApplicationException {
        AdoptionApplication app = adoptionService.submitApplication(PET_ID, ADOPTER_ID);
        HomeVisit scheduled = adoptionService.approveApplication(app.getApplicationId(), "");

        HomeVisit processed = homeVisitService.processNextVisit("Visit went well").orElseThrow();
        assertEquals(scheduled.getVisitId(), processed.getVisitId());
        assertEquals(com.hcl.petadoption.model.HomeVisitStatus.COMPLETED, processed.getStatus());

        // Queue should now be empty
        assertEquals(0, homeVisitService.pendingCount());
        assertTrue(homeVisitService.peekNextVisit().isEmpty());
    }

    @Test
    @DisplayName("processNextVisit on empty queue should return Optional.empty")
    void testDequeueEmptyQueue() {
        assertTrue(homeVisitService.processNextVisit("notes").isEmpty());
    }
}
