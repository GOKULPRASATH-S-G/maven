# AI-USAGE.md – AI-Assisted Engineering Log

This document records authentic examples of AI-generated suggestions, developer architectural reviews, issues detected, corrections made, and the engineering rationale behind each decision during the development of **Assessment 1: Core Java**.

---

## Record 1: Domain Concurrency & Duplicate Adoption Enforcement

- **Component**: `AdoptionService.java`
- **AI-Generated Suggestion**:
  The initial AI scaffold suggested checking whether the pet is currently `AVAILABLE` at the time of application submission, but for application approval, it simply updated the target application's status to `APPROVED` and set `pet.setStatus(PetStatus.ADOPTED)`.
- **Developer Review**:
  Examining the business flow revealed a critical edge case: multiple applicants could submit applications for the same pet while it was `AVAILABLE`. If staff subsequently approved applicant A and later approved applicant B for the same pet without verifying current state, the system would create conflicting adoption records.
- **Issue Discovered**:
  Lack of an approval-time check for existing approved adoptions.
- **Correction Made**:
  Added an explicit validation query before approval:
  ```java
  boolean alreadyAdopted = applicationRepository.findByPetId(pet.getPetId()).stream()
      .anyMatch(app -> app.getStatus() == AdoptionStatus.APPROVED);
  if (alreadyAdopted) {
      throw new DuplicateAdoptionException("Pet " + pet.getName() + " has already been adopted by another applicant.");
  }
  ```
  Created custom unchecked exception `DuplicateAdoptionException` and verified with dedicated JUnit test `testApproveApplication_WhenAlreadyAdopted_ThrowsDuplicateAdoptionException`.
- **Reason for Correction**:
  Enforces strict business integrity at the domain layer regardless of UI sequence.

---

## Record 2: Encapsulation of Mutable Entity State (Pet Traits)

- **Component**: `Pet.java`
- **AI-Generated Suggestion**:
  The AI suggested a standard getter returning the internal `Set<String> traits` collection:
  ```java
  public Set<String> getTraits() {
      return this.traits;
  }
  ```
- **Developer Review**:
  Exposing the reference to the internal `HashSet` allows external callers to mutate the entity's traits directly (e.g., `pet.getTraits().clear()`) bypassing any validation or encapsulation rules.
- **Issue Discovered**:
  Encapsulation leak violating OOP principles.
- **Correction Made**:
  Updated the getter to return an unmodifiable view:
  ```java
  public Set<String> getTraits() {
      return Collections.unmodifiableSet(this.traits);
  }
  ```
  Added explicit domain method `addTrait(String trait)` with null and blank checking.
- **Reason for Correction**:
  Protects internal entity state from unauthorized external mutation while retaining immutability for readers.

---

## Record 3: Home Visit Queue Processing Semantics (Peek vs Poll)

- **Component**: `HomeVisitService.java`
- **AI-Generated Suggestion**:
  The initial suggestion implemented a single `processNextVisit()` method that immediately invoked `pendingVisits.poll()` and returned the visit object without allowing staff to inspect the next visit ahead of time.
- **Developer Review**:
  Shelter staff need the ability to view the upcoming home visit details (adopter address, pet details, scheduled time) before marking the visit as complete. If `poll()` were used to view details, an accidental CLI view would permanently remove the visit from the scheduling queue.
- **Issue Discovered**:
  Destructive read operation preventing inspection of upcoming queue items.
- **Correction Made**:
  Separated queue inspection and completion into distinct methods:
  - `Optional<HomeVisit> peekNextVisit()`: Non-destructive inspection using `pendingVisits.peek()`.
  - `Optional<HomeVisit> processNextVisit(String visitorName, String remarks)`: Destructive dequeue using `pendingVisits.poll()`, which then updates status to `COMPLETED` and archives to `allVisits`.
- **Reason for Correction**:
  Aligns queue data structure semantics (`peek()` vs `poll()`) with real-world administrative workflow requirements.

---

## Record 4: Exception Handling Granularity & Prevention of Blanket Catches

- **Component**: `Main.java` & `ConsoleInput.java`
- **AI-Generated Suggestion**:
  The interactive CLI loop initially contained a top-level `catch (Exception e)` block that swallowed all runtime errors with a generic "Something went wrong" message.
- **Developer Review**:
  HCL assessment standards strictly forbid blanket `catch(Exception)` blocks. Swallowing general exceptions obscures programming errors (such as `NullPointerException`) and prevents custom domain exceptions (`PetNotFoundException`, `AdoptionApplicationException`) from providing actionable feedback to users.
- **Issue Discovered**:
  Violation of clean code principles; swallowed exceptions.
- **Correction Made**:
  Refactored menu action handlers into focused methods catching specific exception types:
  ```java
  try {
      adoptionService.submitApplication(petId, adopterId, notes);
      System.out.println("Application submitted successfully.");
  } catch (PetNotFoundException e) {
      System.err.println("Validation Error: " + e.getMessage());
  } catch (AdoptionApplicationException e) {
      System.err.println("Application Rejected: " + e.getMessage());
  }
  ```
- **Reason for Correction**:
  Ensures descriptive, contextual user feedback and prevents masking of unexpected bugs.

---

## Record 5: Maven Compatibility & Compiler Toolchain Alignment

- **Component**: `pom.xml`
- **AI-Generated Suggestion**:
  Initial POM configured `<java.version>21</java.version>` and `--enable-preview` compiler arguments.
- **Developer Review**:
  System environment check showed the host runtime has Java 17 LTS installed (`javac 17.0.17`). Building with Java 21 configuration caused immediate bytecode target incompatibilities on the local environment.
- **Issue Discovered**:
  Toolchain version mismatch between POM target and host JDK.
- **Correction Made**:
  Updated POM configuration to:
  ```xml
  <properties>
      <java.version>17</java.version>
      <maven.compiler.source>17</maven.compiler.source>
      <maven.compiler.target>17</maven.compiler.target>
  </properties>
  ```
  Ensured all code uses standard Java 17 features (records/sealed classes/pattern matching preview flags removed to ensure 100% vanilla LTS compatibility).
- **Reason for Correction**:
  Guarantees deterministic, error-free compilation on standard enterprise LTS JDK 17 environments.
