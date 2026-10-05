package com.hcl.petadoption;

import com.hcl.petadoption.exception.AdoptionApplicationException;
import com.hcl.petadoption.exception.DuplicateAdoptionException;
import com.hcl.petadoption.exception.PetNotFoundException;
import com.hcl.petadoption.model.Adopter;
import com.hcl.petadoption.model.AdoptionApplication;
import com.hcl.petadoption.model.Cat;
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
import com.hcl.petadoption.util.ConsoleInput;
import com.hcl.petadoption.util.SampleDataLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Entry point for the Pet Adoption and Veterinary Clinic Portal (Assessment 1).
 *
 * <p>Architecture:
 * <pre>
 *   Main (UI / Console)
 *        |
 *   PetService / AdoptionService / HomeVisitService
 *        |
 *   PetRepository / AdoptionApplicationRepository / AdopterRepository
 *        |
 *   In-memory collections (List, Map, Set, Queue)
 * </pre>
 *
 * <p>Demonstrates try-with-resources via {@link ConsoleInput}.
 */
public class Main {

    // -------------------------------------------------------------------------
    // ANSI colour helpers (degrades gracefully on terminals without ANSI support)
    // -------------------------------------------------------------------------
    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";

    // -------------------------------------------------------------------------
    // Services – wired manually (DI container added in Assessment 3)
    // -------------------------------------------------------------------------
    private final PetRepository                  petRepository;
    private final AdoptionApplicationRepository  appRepository;
    private final AdopterRepository              adopterRepository;
    private final PetService                     petService;
    private final HomeVisitService               homeVisitService;
    private final AdoptionService                adoptionService;

    public Main() {
        this.petRepository       = new PetRepository();
        this.appRepository       = new AdoptionApplicationRepository();
        this.adopterRepository   = new AdopterRepository();
        this.petService          = new PetService(petRepository);
        this.homeVisitService    = new HomeVisitService();
        this.adoptionService     = new AdoptionService(
                appRepository, adopterRepository, petService, homeVisitService);
    }

    // -------------------------------------------------------------------------
    // Application entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        Main app = new Main();
        SampleDataLoader.load(app.petService, app.adopterRepository);
        app.run();
    }

    /**
     * Main loop.  Uses try-with-resources to ensure {@link ConsoleInput} is closed.
     */
    private void run() {
        // try-with-resources – ConsoleInput implements Closeable
        try (ConsoleInput input = new ConsoleInput()) {
            printBanner();
            boolean running = true;
            while (running) {
                printMainMenu();
                int choice = input.readRequiredInt("  Enter choice: ");
                System.out.println();
                switch (choice) {
                    case 1  -> registerPet(input);
                    case 2  -> listPets();
                    case 3  -> searchFilterPets(input);
                    case 4  -> sortPets(input);
                    case 5  -> applyForAdoption(input);
                    case 6  -> viewApplications(input);
                    case 7  -> reviewApplication(input);
                    case 8  -> viewHomeVisits();
                    case 9  -> processHomeVisit(input);
                    case 0  -> running = false;
                    default -> System.out.println(RED + "  [!] Invalid option. Please choose 0-9." + RESET);
                }
            }
            System.out.println(GREEN + "\n  Thank you for using the Pet Adoption Portal. Goodbye!" + RESET);
        } catch (java.io.IOException e) {
            System.err.println(RED + "[FATAL] I/O error occurred: " + e.getMessage() + RESET);
        }
    }

    // =========================================================================
    // FLOW 1 – PET REGISTRATION & LISTING
    // =========================================================================

    /** Menu option 1 – Register a new pet. */
    private void registerPet(ConsoleInput input) {
        printSectionHeader("Register New Pet");

        // Species selection
        System.out.println("  Select species:");
        System.out.println("    1. Dog");
        System.out.println("    2. Cat");
        int speciesChoice = input.readRequiredInt("  Choice: ");
        if (speciesChoice != 1 && speciesChoice != 2) {
            System.out.println(RED + "  [!] Invalid species selection." + RESET);
            return;
        }

        String petId = input.readRequiredLine("  Pet ID           : ");
        if (petService.findPetById(petId).isPresent()) {
            System.out.println(RED + "  [!] A pet with ID '" + petId + "' already exists." + RESET);
            return;
        }

        String name      = input.readRequiredLine("  Name             : ");
        int    age       = input.readRequiredInt("  Age (years)      : ");
        String breed     = input.readRequiredLine("  Breed            : ");

        System.out.println("  Gender – 1=Male 2=Female 3=Unknown");
        int genderChoice = input.readRequiredInt("  Choice           : ");
        Gender gender = switch (genderChoice) {
            case 1 -> Gender.MALE;
            case 2 -> Gender.FEMALE;
            default -> Gender.UNKNOWN;
        };

        String traitsInput = input.readLine("  Traits (comma-separated, optional): ");

        Pet pet;
        if (speciesChoice == 1) {
            boolean servicedog = input.readConfirmation("  Trained for service?");
            pet = new Dog(petId, name, age, breed, gender, PetStatus.AVAILABLE, servicedog);
        } else {
            boolean indoorOnly = input.readConfirmation("  Indoor only?");
            pet = new Cat(petId, name, age, breed, gender, PetStatus.AVAILABLE, indoorOnly);
        }

        if (!traitsInput.isBlank()) {
            Arrays.stream(traitsInput.split(","))
                  .forEach(pet::addTrait);
        }

        try {
            petService.registerPet(pet);
            System.out.println(GREEN + "\n  Pet registered successfully!" + RESET);
            System.out.println(pet.getSummary());
        } catch (IllegalArgumentException e) {
            System.out.println(RED + "  [!] " + e.getMessage() + RESET);
        }
    }

    /** Menu option 2 – List all pets. */
    private void listPets() {
        printSectionHeader("All Pets (sorted by name)");
        List<Pet> pets = petService.listAllPets();
        if (pets.isEmpty()) {
            System.out.println("  No pets registered yet.");
            return;
        }
        pets.forEach(p -> {
            System.out.println();
            System.out.println(p.getSummary());
            System.out.println("  " + "-".repeat(50));
        });
        System.out.println("\n" + petService.getStatusSummary());
    }

    /** Menu option 3 – Search / filter pets. */
    private void searchFilterPets(ConsoleInput input) {
        printSectionHeader("Search / Filter Pets");
        System.out.println("  1. Filter by status");
        System.out.println("  2. Filter by species");
        System.out.println("  3. Filter by traits");
        System.out.println("  4. Find by ID");

        int choice = input.readRequiredInt("  Choice: ");
        System.out.println();

        List<Pet> results = switch (choice) {
            case 1 -> {
                System.out.println("  Statuses: 1=AVAILABLE 2=PENDING 3=ADOPTED 4=UNAVAILABLE");
                int sc = input.readRequiredInt("  Choice: ");
                PetStatus status = switch (sc) {
                    case 2 -> PetStatus.PENDING;
                    case 3 -> PetStatus.ADOPTED;
                    case 4 -> PetStatus.UNAVAILABLE;
                    default -> PetStatus.AVAILABLE;
                };
                yield petService.filterByStatus(status);
            }
            case 2 -> {
                System.out.println("  Species: 1=Dog  2=Cat");
                int sc = input.readRequiredInt("  Choice: ");
                List<? extends Pet> speciesList = (sc == 1)
                        ? petService.filterBySpecies(Dog.class)
                        : petService.filterBySpecies(Cat.class);
                yield new java.util.ArrayList<Pet>(speciesList);
            }
            case 3 -> {
                String t = input.readRequiredLine("  Enter traits (comma-separated): ");
                yield petService.filterByTraits(Arrays.asList(t.split(",")));
            }
            case 4 -> {
                String id = input.readRequiredLine("  Enter Pet ID: ");
                try {
                    Pet p = petService.findPetOrThrow(id);
                    System.out.println(p.getSummary());
                } catch (PetNotFoundException e) {
                    System.out.println(RED + "  [!] " + e.getMessage() + RESET);
                }
                yield List.of();
            }
            default -> {
                System.out.println(RED + "  [!] Invalid choice." + RESET);
                yield List.of();
            }
        };

        if (!results.isEmpty()) {
            System.out.println("\n  Results (" + results.size() + " pet(s)):\n");
            results.forEach(p -> {
                System.out.println(p.getSummary());
                System.out.println("  " + "-".repeat(50));
            });
        } else if (choice != 4) {
            System.out.println("  No pets matched the filter.");
        }
    }

    /** Menu option 4 – Sort pets. */
    private void sortPets(ConsoleInput input) {
        printSectionHeader("Sort Pets");
        System.out.println("  1. Sort by name (natural order – Comparable)");
        System.out.println("  2. Sort by age ascending  (Comparator)");
        System.out.println("  3. Sort by age descending (Comparator)");
        System.out.println("  4. Sort by breed          (Comparator)");
        System.out.println("  5. Sort by status         (Comparator)");

        int choice = input.readRequiredInt("  Choice: ");
        System.out.println();

        List<Pet> sorted = switch (choice) {
            case 1 -> petService.listAllPets();  // natural Comparable order
            case 2 -> petService.sortedBy(PetService.BY_AGE_ASC);
            case 3 -> petService.sortedBy(PetService.BY_AGE_DESC);
            case 4 -> petService.sortedBy(PetService.BY_BREED);
            case 5 -> petService.sortedBy(PetService.BY_STATUS);
            default -> {
                System.out.println(RED + "  [!] Invalid choice." + RESET);
                yield List.of();
            }
        };

        if (!sorted.isEmpty()) {
            System.out.println("  " + sorted.size() + " pet(s):\n");
            sorted.forEach(p -> System.out.println("    " + p));
        }
    }

    // =========================================================================
    // FLOW 2 – ADOPTION APPLICATION & REVIEW
    // =========================================================================

    /** Menu option 5 – Apply for adoption. */
    private void applyForAdoption(ConsoleInput input) {
        printSectionHeader("Apply for Adoption");

        // Show available pets
        List<Pet> available = petService.filterByStatus(PetStatus.AVAILABLE);
        if (available.isEmpty()) {
            System.out.println("  No pets are currently available for adoption.");
            return;
        }
        System.out.println("  Available pets:\n");
        available.forEach(p -> System.out.println("    " + p));

        System.out.println();
        String petId = input.readRequiredLine("  Enter Pet ID to apply for: ");

        System.out.println("\n  Applicant details:");
        String name  = input.readRequiredLine("  Full name : ");
        String email = input.readRequiredLine("  Email     : ");
        String phone = input.readLine("  Phone     : ");

        try {
            Adopter adopter = adoptionService.registerOrFindAdopter(name, email, phone);
            AdoptionApplication app = adoptionService.submitApplication(petId, adopter.getAdopterId());

            System.out.println(GREEN + "\n  Application submitted successfully!" + RESET);
            System.out.println("  Your Application ID: " + BOLD + app.getApplicationId() + RESET);
            System.out.println("  Keep this ID to track your application status.");
            System.out.println("\n" + app.getSummary());

        } catch (PetNotFoundException e) {
            System.out.println(RED + "  [!] Pet not found: " + e.getMessage() + RESET);
        } catch (AdoptionApplicationException e) {
            System.out.println(RED + "  [!] Application failed [" + e.getCode() + "]: " + e.getMessage() + RESET);
        }
    }

    /** Menu option 6 – View submitted applications. */
    private void viewApplications(ConsoleInput input) {
        printSectionHeader("View Applications");
        System.out.println("  1. All applications");
        System.out.println("  2. Applications for a specific pet");
        System.out.println("  3. Applications by adopter ID");

        int choice = input.readRequiredInt("  Choice: ");
        System.out.println();

        List<AdoptionApplication> apps = switch (choice) {
            case 1 -> adoptionService.getAllApplications();
            case 2 -> {
                String petId = input.readRequiredLine("  Enter Pet ID: ");
                yield adoptionService.getApplicationsForPet(petId);
            }
            case 3 -> {
                String adopterId = input.readRequiredLine("  Enter Adopter ID: ");
                yield adoptionService.getApplicationsByAdopter(adopterId);
            }
            default -> {
                System.out.println(RED + "  [!] Invalid choice." + RESET);
                yield List.of();
            }
        };

        if (apps.isEmpty()) {
            System.out.println("  No applications found.");
        } else {
            System.out.println("  Found " + apps.size() + " application(s):\n");
            apps.forEach(a -> {
                System.out.println(a.getSummary());
                // Enrich with pet name using Stream + Optional
                petService.findPetById(a.getPetId())
                        .ifPresent(p -> System.out.println("  Pet             : " + p.getName()));
                adoptionService.findAdopterById(a.getAdopterId())
                        .ifPresent(ad -> System.out.println("  Applicant       : " + ad.getName()));
                System.out.println("  " + "-".repeat(50));
            });
        }
    }

    /** Menu option 7 – Review (approve/reject) an application. */
    private void reviewApplication(ConsoleInput input) {
        printSectionHeader("Review Application");

        String applicationId = input.readRequiredLine("  Enter Application ID: ");

        Optional<AdoptionApplication> appOpt = adoptionService.findApplicationById(applicationId);
        if (appOpt.isEmpty()) {
            System.out.println(RED + "  [!] Application not found: " + applicationId + RESET);
            return;
        }

        AdoptionApplication app = appOpt.get();
        System.out.println("\n  Application details:");
        System.out.println(app.getSummary());

        petService.findPetById(app.getPetId())
                .ifPresent(p -> System.out.println("  Pet             : " + p));

        if (app.isTerminal()) {
            System.out.println(YELLOW + "\n  This application is already in a terminal state: "
                    + app.getStatus().getDisplayName() + RESET);
            return;
        }

        System.out.println("\n  Action: 1=Approve  2=Reject  0=Cancel");
        int action = input.readRequiredInt("  Choice: ");

        if (action == 0) {
            System.out.println("  Review cancelled.");
            return;
        }

        String notes = input.readLine("  Review notes (optional): ");

        try {
            if (action == 1) {
                HomeVisit visit = adoptionService.approveApplication(applicationId, notes);
                System.out.println(GREEN + "\n  Application APPROVED!" + RESET);
                System.out.println("  Home visit scheduled:");
                System.out.println(visit.getSummary());
            } else if (action == 2) {
                adoptionService.rejectApplication(applicationId, notes);
                System.out.println(YELLOW + "\n  Application REJECTED." + RESET);
            } else {
                System.out.println(RED + "  [!] Invalid action." + RESET);
            }
        } catch (DuplicateAdoptionException e) {
            System.out.println(RED + "\n  [!] Business Rule Violation: " + e.getMessage() + RESET);
        } catch (AdoptionApplicationException e) {
            System.out.println(RED + "\n  [!] Review failed [" + e.getCode() + "]: " + e.getMessage() + RESET);
        }
    }

    /** Menu option 8 – View home visits (queue state). */
    private void viewHomeVisits() {
        printSectionHeader("Home Visit Queue");
        List<HomeVisit> pending = homeVisitService.getPendingVisits();
        System.out.println("  Pending visits in queue: " + pending.size());

        Optional<HomeVisit> next = homeVisitService.peekNextVisit();
        next.ifPresentOrElse(
                v -> {
                    System.out.println("\n  Next visit (Queue.peek):");
                    System.out.println(v.getSummary());
                },
                () -> System.out.println("  Queue is empty.")
        );

        if (pending.size() > 1) {
            System.out.println("\n  All pending visits:");
            pending.forEach(v -> System.out.println("    " + v));
        }

        List<HomeVisit> all = homeVisitService.getAllVisits();
        if (all.size() > pending.size()) {
            System.out.println("\n  Completed visits:");
            all.stream()
               .filter(v -> v.getStatus() != com.hcl.petadoption.model.HomeVisitStatus.SCHEDULED)
               .forEach(v -> System.out.println("    " + v));
        }
    }

    /** Menu option 9 – Process (dequeue) the next home visit. */
    private void processHomeVisit(ConsoleInput input) {
        printSectionHeader("Process Home Visit");

        if (homeVisitService.pendingCount() == 0) {
            System.out.println("  No pending home visits in the queue.");
            return;
        }

        Optional<HomeVisit> next = homeVisitService.peekNextVisit();
        next.ifPresent(v -> {
            System.out.println("  Next visit to process:");
            System.out.println(v.getSummary());
        });

        if (!input.readConfirmation("\n  Process this visit?")) {
            System.out.println("  Processing cancelled.");
            return;
        }

        String notes = input.readLine("  Staff notes: ");
        Optional<HomeVisit> processed = homeVisitService.processNextVisit(notes);

        processed.ifPresentOrElse(
                v -> {
                    System.out.println(GREEN + "\n  Home visit marked as COMPLETED." + RESET);
                    System.out.println(v.getSummary());
                    System.out.println("\n  Remaining in queue: " + homeVisitService.pendingCount());
                },
                () -> System.out.println(RED + "  [!] Queue was already empty." + RESET)
        );
    }

    // =========================================================================
    // Display helpers
    // =========================================================================

    private void printBanner() {
        System.out.println();
        System.out.println(CYAN + BOLD +
                "  ╔══════════════════════════════════════════════╗\n" +
                "  ║    PET ADOPTION & VETERINARY CLINIC PORTAL   ║\n" +
                "  ║              Assessment 1 – Core Java        ║\n" +
                "  ╚══════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    private void printMainMenu() {
        System.out.println(CYAN + "\n  ─── MAIN MENU ───────────────────────────────" + RESET);
        System.out.println("   1. Register Pet");
        System.out.println("   2. List Pets");
        System.out.println("   3. Search / Filter Pets");
        System.out.println("   4. Sort Pets");
        System.out.println("   5. Apply for Adoption");
        System.out.println("   6. View Applications");
        System.out.println("   7. Review Application  (approve / reject)");
        System.out.println("   8. View Home Visits");
        System.out.println("   9. Process Home Visit  (Queue dequeue)");
        System.out.println("   0. Exit");
        System.out.println(CYAN + "  ─────────────────────────────────────────────" + RESET);
    }

    private void printSectionHeader(String title) {
        System.out.println();
        System.out.println(BOLD + "  ── " + title + " " + "─".repeat(Math.max(0, 44 - title.length())) + RESET);
    }
}
