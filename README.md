# Pet Adoption & Veterinary Clinic Portal

## Project Purpose

A **Pet Adoption and Veterinary Clinic Portal** being built progressively across five HCL Java Full Stack assessment checkpoints.  
This repository contains **Assessment 1 – Core Java**, which implements the foundational domain model and two complete business flows as a menu-driven console application.

The goal of this stage is to establish a clean, maintainable Java architecture that can be evolved — without rewriting business logic — through SQL persistence (Assessment 2), Spring Boot REST APIs (Assessment 3), a React frontend (Assessment 4), and microservices / cloud deployment (Assessment 5).

---

## Assessment 1 Scope

| In Scope                          | Out of Scope (future assessments) |
|-----------------------------------|-----------------------------------|
| Core Java 17                      | Spring / Spring Boot              |
| Maven build                       | MySQL / JPA / Hibernate           |
| OOP – abstraction, inheritance, polymorphism | REST APIs              |
| Generics – generic repository     | React / TypeScript                |
| Collections – List, Set, Map, Queue | JWT / Spring Security           |
| Streams, Optional, java.time      | Microservices / Docker / Kubernetes |
| Custom exceptions (checked + unchecked) | Cloud deployment            |
| JUnit 5 unit tests                |                                   |

---

## Current Features

### Flow 1 – Pet Registration & Listing
- Register Dogs and Cats with all attributes
- Unique trait tracking using `Set<String>`
- List all pets (sorted by name via `Comparable`)
- Filter by status, species, or traits (Streams)
- Sort by age / breed / status (Comparators)
- Polymorphic display – `getSpeciesInfo()` and `makeSound()` differ by species

### Flow 2 – Adoption Application & Review
- View available pets
- Submit an adoption application (checked exception if pet unavailable)
- Unique application ID generated via UUID
- View applications (all / by pet / by adopter)
- Shelter staff approves or rejects an application
- Approval changes pet status to ADOPTED
- Approved adoption schedules a home visit (Queue enqueue)
- Staff views the next visit (Queue peek)
- Staff processes a completed visit (Queue dequeue)
- **Business Rule**: duplicate approved adoption throws `DuplicateAdoptionException`

---

## Technology Used

| Technology  | Version   | Purpose                        |
|-------------|-----------|--------------------------------|
| Java        | 17        | Core language                  |
| Maven       | 3.9.x     | Build and dependency management |
| JUnit 5     | 5.10.2    | Unit testing                   |

---

## Package Structure

```
src/main/java/com/hcl/petadoption/
│
├── Main.java                          ← Entry point / console UI
│
├── model/
│   ├── Pet.java                       ← Abstract base (Comparable<Pet>)
│   ├── Dog.java                       ← Concrete subclass
│   ├── Cat.java                       ← Concrete subclass
│   ├── Adopter.java                   ← Adopter domain entity
│   ├── AdoptionApplication.java       ← Application domain entity
│   ├── HomeVisit.java                 ← HomeVisit domain entity
│   ├── PetStatus.java                 ← Enum: AVAILABLE, PENDING, ADOPTED, UNAVAILABLE
│   ├── AdoptionStatus.java            ← Enum: SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED
│   ├── Gender.java                    ← Enum: MALE, FEMALE, UNKNOWN
│   └── HomeVisitStatus.java           ← Enum: SCHEDULED, COMPLETED, CANCELLED
│
├── repository/
│   ├── Repository.java                ← Generic repository interface <T, ID>
│   ├── InMemoryRepository.java        ← Generic in-memory Map<ID, T> implementation
│   ├── PetRepository.java             ← Concrete pet repository
│   ├── AdoptionApplicationRepository.java ← Concrete application repository
│   └── AdopterRepository.java         ← Concrete adopter repository
│
├── service/
│   ├── PetService.java                ← Pet business logic; implements PetOperations
│   ├── AdoptionService.java           ← Adoption workflow business logic
│   └── HomeVisitService.java          ← Queue-based home visit management
│
├── exception/
│   ├── AdoptionApplicationException.java  ← Checked exception
│   ├── PetNotFoundException.java          ← Unchecked exception
│   └── DuplicateAdoptionException.java    ← Unchecked business rule exception
│
├── interfaces/
│   └── PetOperations.java             ← Pet management contract (interface)
│
└── util/
    ├── ConsoleInput.java              ← Validated console input (Closeable)
    └── SampleDataLoader.java          ← Pre-loaded demo data

src/test/java/com/hcl/petadoption/
├── PetServiceTest.java                ← Pet registration, filtering, sorting tests
└── AdoptionServiceTest.java           ← Adoption workflow and queue behaviour tests
```

---

## How to Build

```bash
mvn clean package
```

The executable JAR is produced at:
```
target/pet-adoption-portal.jar
```

---

## How to Run

```bash
java -jar target/pet-adoption-portal.jar
```

Sample data (2 dogs, 2 cats, 2 adopters) is loaded automatically on startup.

---

## How to Test

```bash
mvn clean test
```

All 20+ JUnit 5 tests should pass.

---

## Sample Flow – Quick Demo

### Flow 1: List pets, sort by age
1. Start the application
2. Choose **2** → List Pets (sorted alphabetically)
3. Choose **4** → Sort Pets → **2** (by age ascending)

### Flow 2: Full adoption cycle
1. Choose **5** → Apply for Adoption
   - Pet ID: `DOG-001`
   - Enter your name, email, phone
   - Note down the Application ID
2. Choose **7** → Review Application
   - Enter the Application ID
   - Choose **1** (Approve)
3. Choose **8** → View Home Visits (visit is in the queue)
4. Choose **9** → Process Home Visit (dequeue and mark complete)

---

## Current Limitations

- No persistence – all data is lost on application restart
- No authentication – anyone can perform any action
- No REST API – console-only interface
- No email notifications
- Veterinary appointments and vaccinations not yet implemented (Assessment 3+)
- No multi-user concurrency support

---

## What's Next (Assessment 2)

- Design a MySQL relational schema based on the current domain model
- Create DDL for `pet`, `adopter`, `adoption_application`, `home_visit` tables
- Write SQL queries, views, and indexes
- Migrate sample data to SQL scripts

See `FUTURE-ARCHITECTURE.md` for the full evolution roadmap.
