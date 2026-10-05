# FUTURE-ARCHITECTURE.md – Roadmap & Architectural Evolution

This document outlines the progressive evolution of the **Pet Adoption and Veterinary Clinic Portal** across the five HCL assessment checkpoints and future enterprise readiness topics.

> [!NOTE]
> None of the future technologies described below are implemented in Assessment 1. This document serves as an architectural design blueprint to ensure that the Core Java domain model and services built in Assessment 1 can transition seamlessly into subsequent tiers without requiring business logic rewrites.

---

## 1. Evolution Roadmap Across Checkpoints

```
Assessment 1 (Current)
   [Core Java Domain & Business Logic]
   [In-Memory Collections: List, Set, Map, Queue]
   [Console CLI Interface]
          │
          ▼
Assessment 2
   [SQL Database Schema & Relational Mapping]
   [DDL/DML: pet, adopter, adoption_application, home_visit]
   [Normalization (3NF), Foreign Keys, Indexes, Views]
          │
          ▼
Assessment 3
   [Spring Boot RESTful APIs]
   [Spring Data JPA replacing In-Memory Repositories]
   [Spring Security & JWT Authentication]
   [Veterinary Module Introduction]
          │
          ▼
Assessment 4
   [React 18 + TypeScript Frontend]
   [Axios / React Query Client Layer]
   [Tailwind / Responsive UI Component Library]
          │
          ▼
Assessment 5 (Capstone Integration)
   [Full-Stack Integration & Packaging]
   [End-to-End User Adoption & Vet Appointment Flows]
   [Automated Test Coverage & Performance Polish]
          │
          ▼
Post-Capstone Enterprise Readiness
   [Spring Cloud Microservices: Adoption Service, Vet Service, Auth Service]
   [API Gateway (Spring Cloud Gateway) & Service Registry (Eureka)]
   [Declarative REST Clients (OpenFeign)]
   [Containerization (Docker), Orchestration (Kubernetes), CI/CD & Cloud Deployment]
```

---

## 2. Checkpoint Details

### Assessment 1: Core Java (Current State)
- **Layer**: Standalone Domain & Service Layer.
- **Persistence**: Generic in-memory repository (`InMemoryRepository<T, ID>`) backed by `LinkedHashMap`.
- **Interface**: Console-driven interactive menu.
- **Key Entities**:
  - `Pet` (Abstract base: `Dog`, `Cat`)
  - `Adopter`
  - `AdoptionApplication`
  - `HomeVisit`
- **Domain Rules**:
  - Pet status transitions: `AVAILABLE` -> `PENDING` -> `ADOPTED`.
  - Duplicate approved adoption prevention via `DuplicateAdoptionException`.
  - FIFO scheduling and processing of home visits via `Queue<HomeVisit>`.

### Assessment 2: SQL Persistence
- **Objective**: Design and implement a normalized relational database schema (MySQL/PostgreSQL) mirroring the domain model.
- **Tables & Relationships**:
  - `pet`: `pet_id` (PK), `name`, `species` (DOG/CAT), `age`, `breed`, `gender`, `status`, `is_trained_for_service`, `is_indoor_only`.
  - `pet_trait`: `pet_id` (FK), `trait` (Composite PK / normalized 1-to-many relationship for `Set<String> traits`).
  - `adopter`: `adopter_id` (PK), `full_name`, `email` (UNIQUE), `phone_number`, `address`.
  - `adoption_application`: `application_id` (PK), `pet_id` (FK), `adopter_id` (FK), `status`, `submission_date`, `notes`.
  - `home_visit`: `visit_id` (PK), `application_id` (FK), `scheduled_date_time`, `status`, `visitor_name`, `remarks`.
- **Integrity & Indexing**:
  - Foreign keys with referential actions.
  - Unique constraint on `(pet_id, status)` where status = 'APPROVED' to enforce the single-adoption business rule at the database level.
  - Indexes on search columns (`pet.status`, `pet.breed`, `adoption_application.status`).
- **Data Migration**: DDL scripts and DML seed scripts based on `SampleDataLoader`.

### Assessment 3: Spring Boot REST Backend
- **Objective**: Transition to an enterprise-grade RESTful web service.
- **Architectural Adaptations**:
  - `PetOperations` and `AdoptionService` will be annotated with `@Service`.
  - `Repository<T, ID>` interfaces will extend `org.springframework.data.jpa.repository.JpaRepository<T, ID>`.
  - In-memory maps will be fully replaced with Hibernate / JPA entity mappings without altering method contracts.
  - REST controllers:
    - `@RestController @RequestMapping("/api/v1/pets")`
    - `@RestController @RequestMapping("/api/v1/applications")`
    - `@RestController @RequestMapping("/api/v1/home-visits")`
  - Validation: Hibernate Validator annotations (`@NotNull`, `@NotBlank`, `@Email`, `@Min`, `@Max`).
  - Security: Spring Security filter chain with stateless JWT authentication and role-based authorization (`ROLE_ADOPTER`, `ROLE_SHELTER_STAFF`, `ROLE_VET`).
  - Exception Handling: `@RestControllerAdvice` mapping domain exceptions (`PetNotFoundException`, `DuplicateAdoptionException`, `AdoptionApplicationException`) to HTTP 404, 409, and 400 status codes.
  - Domain Extension: Addition of the Veterinary Clinic sub-domain (appointments, vaccination schedules, medical histories).

### Assessment 4: React + TypeScript Frontend
- **Objective**: Modern, responsive Single-Page Application (SPA).
- **Key Modules**:
  - Public Pet Catalog: Grid view with live search, species filter (Dog/Cat), status filter, and trait tags.
  - Adoption Portal: Multi-step interactive adoption application form with client-side validation.
  - Shelter Staff Dashboard: Application review queue with approve/reject actions and real-time status updates.
  - Veterinary Management: Appointment booking calendar, patient check-in, and vaccination logs.
- **Tech Stack**:
  - React 18 / Vite / TypeScript.
  - State management & caching via TanStack Query (React Query) and Context API / Zustand.
  - Axios HTTP client configured with JWT interceptors.

### Assessment 5: Final Full-Stack Integration & Capstone
- **Objective**: Seamless end-to-end integration of backend, database, and frontend.
- **Key Deliverables**:
  - End-to-end user workflows tested from UI to database.
  - CORS configuration, unified API error handling, and session management.
  - Comprehensive automated test suite: backend unit + integration tests (MockMvc, Testcontainers) and frontend component/E2E tests.
  - Production packaging and runnable environment verification.

---

## 3. Beyond Capstone: Enterprise & Cloud-Native Horizons

### Microservices Decomposition
When scaling the application under high traffic, the monolithic Spring Boot service will be decoupled into independent bounded contexts:
1. `identity-service`: User management, JWT token generation, OAuth2 / OIDC.
2. `pet-catalog-service`: Pet registration, search, filtering, and trait management.
3. `adoption-service`: Application submission, staff review, home visit queue orchestration.
4. `veterinary-service`: Clinical appointments, vaccination records, and doctor scheduling.
5. `notification-service`: Asynchronous email/SMS updates via message queues (RabbitMQ / Apache Kafka).

### Infrastructure & Cloud Architecture
- **Service Discovery**: Spring Cloud Netflix Eureka or Consul for dynamic service registration.
- **API Gateway**: Spring Cloud Gateway for unified ingress routing, rate limiting, and centralized JWT validation.
- **Inter-Service Communication**: Declarative REST calls using Spring Cloud OpenFeign and asynchronous event streaming.
- **Containerization & Orchestration**:
  - Multi-stage Dockerfiles for lean, secure container images.
  - Kubernetes manifests (Deployments, Services, ConfigMaps, Ingress, PersistentVolumeClaims) or Helm charts.
- **CI/CD Pipeline**: GitHub Actions / GitLab CI for automated linting, unit testing, container build, and deployment to cloud environments (AWS / GCP / Azure).
