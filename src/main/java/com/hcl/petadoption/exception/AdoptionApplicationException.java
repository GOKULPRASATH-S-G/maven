package com.hcl.petadoption.exception;

/**
 * Thrown for invalid adoption application operations such as:
 * <ul>
 *   <li>Applying for a pet that is not available (PENDING or ADOPTED)</li>
 *   <li>Providing invalid applicant data</li>
 *   <li>Transitioning an application to an illegal state</li>
 * </ul>
 *
 * <p>This is a <b>checked exception</b> ({@link Exception} subclass) because
 * callers are expected to handle invalid-application scenarios explicitly –
 * the console UI should present a meaningful error message to the user.
 *
 * <p>Demonstrates: custom checked exception, {@code throws} declaration,
 * {@code try/catch} in service layer.
 */
public class AdoptionApplicationException extends Exception {

    private final String code;

    public AdoptionApplicationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AdoptionApplicationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Returns a short error code for programmatic handling (e.g. "PET_NOT_AVAILABLE",
     * "INVALID_STATE", "APPLICANT_DATA_INVALID").  Will map to error codes in the
     * REST API error response body in Assessment 3.
     */
    public String getCode() {
        return code;
    }
}
