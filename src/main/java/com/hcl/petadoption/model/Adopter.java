package com.hcl.petadoption.model;

/**
 * Represents a person who wishes to adopt a pet.
 *
 * <p>Encapsulation is demonstrated through private fields and validated setters.
 * The {@code adopterId} is a stable surrogate key that will map to the primary key
 * of the {@code adopter} table in Assessment 2.
 *
 * <p>Email is used as a natural unique key (unique constraint in the DB later).
 */
public class Adopter {

    private final String adopterId;
    private String name;
    private String email;
    private String phone;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new Adopter with all required fields.
     *
     * @param adopterId stable unique identifier (never null or blank)
     * @param name      applicant full name
     * @param email     contact email – will serve as a natural unique key
     * @param phone     contact phone number
     */
    public Adopter(String adopterId, String name, String email, String phone) {
        if (adopterId == null || adopterId.isBlank()) {
            throw new IllegalArgumentException("Adopter ID must not be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Adopter name must not be blank.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Adopter email is invalid.");
        }
        this.adopterId = adopterId;
        this.name      = name;
        this.email     = email;
        this.phone     = phone;
    }

    // -------------------------------------------------------------------------
    // Accessors / Mutators
    // -------------------------------------------------------------------------

    public String getAdopterId() { return adopterId; }

    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank.");
        }
        this.name = name;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        this.email = email;
    }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    public String getSummary() {
        return String.format(
                "  Adopter ID : %s%n" +
                "  Name       : %s%n" +
                "  Email      : %s%n" +
                "  Phone      : %s",
                adopterId, name, email, phone);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s <%s>", adopterId, name, email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Adopter other)) return false;
        return adopterId.equals(other.adopterId);
    }

    @Override
    public int hashCode() {
        return adopterId.hashCode();
    }
}
