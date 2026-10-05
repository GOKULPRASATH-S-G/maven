# JAVA-MEMORY.md – JVM Memory Management & Pitfalls

This document explains the runtime memory model of the Java Virtual Machine (JVM) using concrete examples from the **Pet Adoption & Veterinary Clinic Portal** codebase.

---

## 1. Stack vs Heap Memory

The JVM divides memory into several runtime data areas. The two most critical areas for application developers are the **Stack** and the **Heap**.

| Characteristic | Stack Memory | Heap Memory |
| :--- | :--- | :--- |
| **Purpose** | Stores method call frames, local primitive variables, and references to objects. | Stores all dynamically allocated objects and instances of classes. |
| **Lifecycle** | Tied strictly to method invocation; allocated on method entry and popped upon return. | Managed automatically by the Garbage Collector (GC); persists as long as reachable. |
| **Thread Safety** | Private to each thread; inherently thread-safe. | Shared among all threads; requires synchronization for concurrent access. |
| **Access Speed** | Fast, sequential LIFO (Last-In-First-Out) pointer adjustments. | Slightly slower due to dynamic allocation, fragmentation, and pointer dereferencing. |
| **Size Limit** | Relatively small (configured via `-Xss`). | Configurable and significantly larger (configured via `-Xms` and `-Xmx`). |

---

## 2. Object Allocation & References (Code Example)

Consider the following operation from `AdoptionService.java`:

```java
public AdoptionApplication submitApplication(String petId, String adopterId, String notes)
        throws AdoptionApplicationException {
    Pet pet = petRepository.findById(petId)
        .orElseThrow(() -> new PetNotFoundException("Pet with ID " + petId + " not found."));

    if (pet.getStatus() != PetStatus.AVAILABLE) {
        throw new AdoptionApplicationException("Pet " + pet.getName() + " is not available for adoption.");
    }

    String applicationId = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    AdoptionApplication application = new AdoptionApplication(
        applicationId, petId, adopterId, AdoptionStatus.SUBMITTED, LocalDateTime.now(), notes
    );

    applicationRepository.save(application);
    return application;
}
```

### Visualizing Memory Layout During Execution

```
+-------------------------------------------------------------+
| STACK FRAME: submitApplication()                            |
|-------------------------------------------------------------|
| [petId]               -> Pointer to String ("DOG-001")      |
| [adopterId]           -> Pointer to String ("ADOPT-001")    |
| [notes]               -> Pointer to String ("Loving home")  |
| [pet]                 -> Pointer to Dog object (Heap 0xA1)  |
| [applicationId]       -> Pointer to String ("APP-3F8E12A4") |
| [application]         -> Pointer to AdoptionApplication     |
|                          object (Heap 0xB2)                 |
+-------------------------------------------------------------+
                            │                  │
                            ▼                  ▼
+-------------------------------------------------------------+
| HEAP MEMORY                                                 |
|-------------------------------------------------------------|
| [0xA1] Dog Instance:                                        |
|   ├── petId: "DOG-001"                                      |
|   ├── name: "Buddy"                                         |
|   ├── age: 3                                                |
|   ├── status: AVAILABLE                                     |
|   ├── isTrainedForService: true                             |
|   └── traits: HashSet Instance [0xC3]                       |
|                                                             |
| [0xB2] AdoptionApplication Instance:                        |
|   ├── applicationId: "APP-3F8E12A4"                         |
|   ├── petId: "DOG-001"                                      |
|   ├── adopterId: "ADOPT-001"                                |
|   ├── status: SUBMITTED                                     |
|   ├── submissionDate: LocalDateTime [0xD4]                  |
|   └── notes: "Loving home"                                  |
|                                                             |
| [0xE5] InMemoryRepository store: LinkedHashMap             |
|   └── Holds strong reference to 0xB2 key-value pair         |
+-------------------------------------------------------------+
```

### Key Takeaways
1. **Local Variables on Stack**: The references `pet`, `applicationId`, and `application` reside in the stack frame of `submitApplication()`.
2. **Objects on Heap**: The actual `Dog`, `AdoptionApplication`, `LocalDateTime`, `String`, and `HashSet` instances reside in the Heap.
3. **Reference Passing**: In Java, arguments are **passed by value**. When passing `petId` or returning `application`, it is the reference (the 32-bit or 64-bit pointer address) that is copied onto the stack, not the underlying object payload.

---

## 3. Java Memory Pitfall: Unintended Retention in In-Memory Collections (Memory Leak)

### The Problem
A common pitfall in in-memory architectures (such as `InMemoryRepository` or `HomeVisitService`) is **unintentional object retention**, frequently referred to as an "in-memory leak".

In Java, an object is eligible for garbage collection only when there are no **strong references** reachable from the GC Roots (active threads, static references, live call stack variables).

In `InMemoryRepository.java`:
```java
public abstract class InMemoryRepository<T, ID> implements Repository<T, ID> {
    private final Map<ID, T> store = new LinkedHashMap<>();
    
    @Override
    public T save(T entity) {
        ID id = extractId(entity);
        store.put(id, entity);
        return entity;
    }
}
```

### How the Pitfall Occurs
- If the application runs as a long-lived service and entities (e.g., thousands of rejected applications or completed home visits) are continually inserted into `store` without an explicit eviction strategy or `deleteById()` mechanism, the internal hash table keeps strong references to all those objects indefinitely.
- Even if callers finish processing, the objects remain reachable from `store`, preventing the garbage collector from reclaiming their heap memory. Over time, this leads to continuous Heap growth and eventual `java.lang.OutOfMemoryError: Java heap space`.

### Mitigation in Our Architecture
1. **Explicit Deletion Contract**: The `Repository` interface defines `deleteById(ID id)`, allowing callers to explicitly dereference entities.
2. **Defensive Copies & Unmodifiable Views**: In `Pet.java`, traits are exposed via `Collections.unmodifiableSet(traits)` rather than returning the raw internal `Set`, preventing callers from holding external references that could prevent localized lifecycle management.
3. **Queue Polling**: In `HomeVisitService`, completed visits are actively removed from the active queue using `pendingVisits.poll()`, allowing transient queue entries to be collected once archived.
4. **Transition to Database in Assessment 2**: In Assessment 2, moving state to a relational database moves retention management out of JVM heap into disk-backed, transactional tables managed with pagination and SQL queries.
