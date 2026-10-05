# JAVA-CONCEPTS.md – Concept-to-Code Mapping

This document maps every required Java concept to the exact class and method where it is demonstrated in Assessment 1.

---

## OOP Concepts

### Encapsulation
| Concept | File | Detail |
|---------|------|--------|
| Private fields + validated setters | `Pet.java` | All fields private; `setName`, `setAge` validate before assignment |
| Private fields + validated setters | `Adopter.java` | `setEmail` validates `@` presence |
| Internal collection hidden | `InMemoryRepository.java` | `store` map is private; exposed only as unmodifiable via `getStore()` |
| Queue not exposed | `HomeVisitService.java` | `pendingVisits` queue is private; accessed via `peek`, `poll`, `offer` methods |

### Abstraction
| Concept | File | Detail |
|---------|------|--------|
| Abstract class | `Pet.java` | Declared `abstract`; cannot be instantiated |
| Abstract methods | `Pet.java` | `getSpeciesInfo()` and `makeSound()` are abstract |
| Interface | `PetOperations.java` | Declares the contract for pet operations |
| Generic abstraction | `Repository.java` | Type-parameterised interface `Repository<T, ID>` |

### Inheritance
| Concept | File | Detail |
|---------|------|--------|
| Extends abstract class | `Dog.java` | `extends Pet` |
| Extends abstract class | `Cat.java` | `extends Pet` |
| Extends generic base | `PetRepository.java` | `extends InMemoryRepository<Pet, String>` |
| Extends generic base | `AdoptionApplicationRepository.java` | `extends InMemoryRepository<AdoptionApplication, String>` |
| Implements interface | `PetService.java` | `implements PetOperations` |
| Implements interface | `InMemoryRepository.java` | `implements Repository<T, ID>` |
| Extends RuntimeException | `PetNotFoundException.java` | Custom unchecked exception |
| Extends RuntimeException | `DuplicateAdoptionException.java` | Custom unchecked business exception |
| Extends Exception | `AdoptionApplicationException.java` | Custom checked exception |

### Polymorphism
| Concept | File | Detail |
|---------|------|--------|
| Method overriding | `Dog.java` | `getSpeciesInfo()` returns dog-specific text |
| Method overriding | `Dog.java` | `makeSound()` returns "Woof! Woof!" |
| Method overriding | `Cat.java` | `getSpeciesInfo()` returns cat-specific text |
| Method overriding | `Cat.java` | `makeSound()` returns "Meow~" |
| Polymorphic method call | `Pet.java::getSummary()` | Calls `getSpeciesInfo()` and `makeSound()` – dispatched to Dog or Cat at runtime |
| Polymorphic collection | `PetRepository.java` | `List<Pet>` stores both Dog and Cat instances |

### Constructors / this / super
| Concept | File | Detail |
|---------|------|--------|
| Constructor chaining – this | `Dog.java` | Convenience constructor calls `this(petId, name, age, breed, gender, status, false)` |
| Constructor chaining – this | `Cat.java` | Convenience constructor calls `this(petId, name, age, breed, gender, status, true)` |
| super keyword | `Dog.java` | Full constructor calls `super(petId, name, age, breed, gender, status)` |
| super keyword | `Cat.java` | Full constructor calls `super(petId, name, age, breed, gender, status)` |
| this keyword | `Dog.java` | `this.isTrainedForService = isTrainedForService` in full constructor |

### Access Modifiers
| Modifier | Usage |
|----------|-------|
| `private` | All entity fields in Pet, Adopter, AdoptionApplication, HomeVisit |
| `protected` | `Pet` constructor (only subclasses can call it); `InMemoryRepository.extractId()` |
| `public` | All service methods, repository interface methods |
| `final` | `petId`, `adopterId`, `applicationId`, `visitId` (immutable stable IDs) |

---

## Collections

### List
| Usage | File | Detail |
|-------|------|--------|
| `List<Pet>` via repository | `InMemoryRepository.java` | `findAll()` returns `List<T>` |
| `List<Pet>` stream results | `PetService.java` | `listAllPets()`, `filterByStatus()`, `sortedBy()` collect to List |
| `List<HomeVisit>` all visits | `HomeVisitService.java` | `allVisits` is a `LinkedList<HomeVisit>` |

### Set
| Usage | File | Detail |
|-------|------|--------|
| `Set<String>` pet traits | `Pet.java` | `private final Set<String> traits = new HashSet<>()` |
| Unique trait enforcement | `Pet.java::addTrait()` | HashSet silently ignores duplicates |
| Unmodifiable view | `Pet.java::getTraits()` | Returns `Collections.unmodifiableSet(traits)` |

### Map
| Usage | File | Detail |
|-------|------|--------|
| `Map<ID, T>` entity store | `InMemoryRepository.java` | `private final Map<ID, T> store = new LinkedHashMap<>()` |
| O(1) lookup | `InMemoryRepository.java::findById()` | `store.get(id)` |
| Count by status | `PetService.java::getStatusSummary()` | `Collectors.groupingBy` produces `Map<PetStatus, Long>` |

### Queue
| Usage | File | Detail |
|-------|------|--------|
| `Queue<HomeVisit>` FIFO | `HomeVisitService.java` | `private final Queue<HomeVisit> pendingVisits = new LinkedList<>()` |
| Enqueue | `HomeVisitService.java::scheduleVisit()` | `pendingVisits.offer(visit)` |
| Peek (non-destructive) | `HomeVisitService.java::peekNextVisit()` | `pendingVisits.peek()` |
| Dequeue | `HomeVisitService.java::processNextVisit()` | `pendingVisits.poll()` |

---

## Generics

### Generic Repository Interface
| Concept | File | Detail |
|---------|------|--------|
| Two-parameter generic interface | `Repository.java` | `Repository<T, ID>` |
| Generic method return types | `Repository.java` | `T save(T entity)`, `Optional<T> findById(ID id)`, `List<T> findAll()` |
| Abstract generic class | `InMemoryRepository.java` | `abstract class InMemoryRepository<T, ID> implements Repository<T, ID>` |
| Concrete specialisation | `PetRepository.java` | `extends InMemoryRepository<Pet, String>` |
| Bounded generic method | `PetRepository.java::findBySpecies()` | `<T extends Pet> List<T> findBySpecies(Class<T> speciesClass)` |
| Bounded generic in interface | `PetOperations.java::filterBySpecies()` | `<T extends Pet> List<T> filterBySpecies(Class<T> speciesClass)` |

---

## Comparable

| Concept | File | Detail |
|---------|------|--------|
| Implements `Comparable<Pet>` | `Pet.java` | Class declaration |
| `compareTo` implementation | `Pet.java::compareTo()` | `this.name.compareToIgnoreCase(other.name)` |
| Used for natural sort | `PetService.java::listAllPets()` | `.stream().sorted()` uses `compareTo` |

---

## Comparator

| Concept | File | Detail |
|---------|------|--------|
| `Comparator<Pet>` by age asc | `PetService.java::BY_AGE_ASC` | `Comparator.comparingInt(Pet::getAge)` |
| `Comparator<Pet>` by age desc | `PetService.java::BY_AGE_DESC` | `BY_AGE_ASC.reversed()` |
| `Comparator<Pet>` by breed | `PetService.java::BY_BREED` | `Comparator.comparing(Pet::getBreed, CASE_INSENSITIVE_ORDER)` |
| `Comparator<Pet>` by status | `PetService.java::BY_STATUS` | `Comparator.comparing(p -> p.getStatus().getDisplayName())` |
| Applied via stream | `PetService.java::sortedBy()` | `.stream().sorted(comparator)` |
| Applied in console | `Main.java::sortPets()` | User chooses a Comparator; petService.sortedBy() is called |

---

## Streams

| Operation | File | Detail |
|-----------|------|--------|
| `filter` | `PetRepository.java::findByStatus()` | Filter by status enum |
| `filter` + `map` + `collect` | `PetRepository.java::findBySpecies()` | Species filtering with cast |
| `filter` – trait matching | `PetRepository.java::findByTraits()` | `p.getTraits().containsAll(normalised)` |
| `sorted()` – natural | `PetService.java::listAllPets()` | Comparable-based sort |
| `sorted(comparator)` | `PetService.java::sortedBy()` | Comparator-based sort |
| `groupingBy` + `counting` | `PetService.java::getStatusSummary()` | Produces `Map<PetStatus, Long>` |
| `findFirst` | `AdoptionApplicationRepository.java::findApprovedApplicationForPet()` | Returns first approved application |
| `anyMatch` | `AdoptionService.java::rejectApplication()` | Checks if other pending apps exist |
| `stream()` on array | `Main.java::registerPet()` | `Arrays.stream(traitsInput.split(",")).forEach(pet::addTrait)` |

---

## Optional

| Usage | File | Detail |
|-------|------|--------|
| Return type for nullable entity | `Repository.java::findById()` | `Optional<T>` |
| Return type for pet lookup | `PetOperations.java::findPetById()` | `Optional<Pet>` |
| `orElseThrow` | `PetService.java::findPetOrThrow()` | Throws `PetNotFoundException` if empty |
| `ifPresent` | `Main.java::viewApplications()` | Enrich display with pet/adopter name |
| `ifPresentOrElse` | `Main.java::viewHomeVisits()` | Show next visit or "Queue is empty" |
| `isEmpty()` check | `AdoptionService.java` | Check for existing approved application |
| `empty()` return | `HomeVisitService.java::processNextVisit()` | Returns empty when queue is empty |

---

## java.time

| Usage | File | Detail |
|-------|------|--------|
| Application date | `AdoptionApplication.java` | `private final LocalDate applicationDate` |
| Default to today | `AdoptionApplication.java` | `LocalDate.now()` |
| Home visit scheduled time | `HomeVisit.java` | `private LocalDateTime scheduledDateTime` |
| Default visit time | `HomeVisit.java` | `LocalDateTime.now().plusDays(7)` |
| Visit scheduling | `AdoptionService.java::approveApplication()` | `LocalDateTime.now().plusDays(7)` |

---

## Exceptions

### Checked Exception
| Concept | File | Detail |
|---------|------|--------|
| Class extends Exception | `AdoptionApplicationException.java` | Custom checked exception |
| `throws` declaration | `AdoptionService.java::submitApplication()` | `throws AdoptionApplicationException` |
| `throws` declaration | `AdoptionService.java::approveApplication()` | `throws AdoptionApplicationException` |
| `try/catch` | `Main.java::applyForAdoption()` | Catches and displays the checked exception |
| Error code field | `AdoptionApplicationException.java` | `getCode()` returns e.g. "PET_NOT_AVAILABLE" |

### Unchecked Exception
| Concept | File | Detail |
|---------|------|--------|
| Extends RuntimeException | `PetNotFoundException.java` | Thrown for unknown pet IDs |
| Extends RuntimeException | `DuplicateAdoptionException.java` | Thrown for duplicate adoption approval |
| `throw` keyword | `PetService.java::findPetOrThrow()` | `throw new PetNotFoundException(petId)` |
| `throw` keyword | `AdoptionService.java::approveApplication()` | `throw new DuplicateAdoptionException(...)` |

### try-with-resources
| Concept | File | Detail |
|---------|------|--------|
| `Closeable` implementation | `ConsoleInput.java` | Wraps `BufferedReader`, implements `close()` |
| `try (ConsoleInput input = ...)` | `Main.java::run()` | ConsoleInput auto-closed on exit or exception |

### finally
| Concept | File | Detail |
|---------|------|--------|
| `finally` block | `ConsoleInput.java::readInt()` | Demonstrates `finally` with an attempt flag |

---

## Enum

| Enum | File | Values |
|------|------|--------|
| `PetStatus` | `PetStatus.java` | AVAILABLE, PENDING, ADOPTED, UNAVAILABLE |
| `AdoptionStatus` | `AdoptionStatus.java` | SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED |
| `Gender` | `Gender.java` | MALE, FEMALE, UNKNOWN |
| `HomeVisitStatus` | `HomeVisitStatus.java` | SCHEDULED, COMPLETED, CANCELLED |
