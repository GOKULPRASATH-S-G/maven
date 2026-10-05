package com.hcl.petadoption.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.OptionalInt;

/**
 * Utility class for validated console input.
 *
 * <p>Implements {@link Closeable} so the underlying {@link BufferedReader} can
 * be properly closed in a <b>try-with-resources</b> block in {@code Main}, demonstrating
 * the try-with-resources Java feature.
 *
 * <p>Demonstrates:
 * <ul>
 *   <li><b>try-with-resources</b> – {@code Closeable} implementation</li>
 *   <li><b>Encapsulation</b>      – input logic isolated from UI and service layers</li>
 *   <li><b>finally</b>            – used in {@code readInt} to demonstrate the keyword</li>
 * </ul>
 */
public class ConsoleInput implements Closeable {

    private final BufferedReader reader;

    public ConsoleInput() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    // -------------------------------------------------------------------------
    // Input methods
    // -------------------------------------------------------------------------

    /**
     * Reads a trimmed line of text from the console.
     *
     * @param prompt the prompt to display before reading
     * @return the trimmed input string, or an empty string on I/O error
     */
    public String readLine(String prompt) {
        System.out.print(prompt);
        try {
            String line = reader.readLine();
            return line != null ? line.trim() : "";
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read input: " + e.getMessage());
            return "";
        }
    }

    /**
     * Reads an integer from the console, returning {@link OptionalInt#empty()} on invalid input.
     * Demonstrates {@code finally} with the "genuine use" of releasing a flag.
     *
     * @param prompt the prompt to display
     * @return the parsed integer, or empty if input was non-numeric
     */
    public OptionalInt readInt(String prompt) {
        System.out.print(prompt);
        boolean attempted = false;
        try {
            attempted = true;
            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(Integer.parseInt(line.trim()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read input: " + e.getMessage());
            return OptionalInt.empty();
        } finally {
            if (!attempted) {
                // This block always executes, ensuring any resource cleanup
                // that depends on the attempt flag is performed.
                System.err.println("[DEBUG] readInt was never attempted.");
            }
        }
    }

    /**
     * Reads an integer with validation loop.  Keeps prompting until valid input.
     *
     * @param prompt the prompt to display
     * @return the valid integer
     */
    public int readRequiredInt(String prompt) {
        while (true) {
            OptionalInt result = readInt(prompt);
            if (result.isPresent()) {
                return result.getAsInt();
            }
            System.out.println("  [!] Please enter a valid number.");
        }
    }

    /**
     * Reads a non-blank string, re-prompting until the user provides one.
     */
    public String readRequiredLine(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("  [!] This field is required.");
        }
    }

    /**
     * Reads a Y/N confirmation from the user.
     *
     * @param prompt the prompt to display
     * @return {@code true} if the user typed 'y' or 'yes'
     */
    public boolean readConfirmation(String prompt) {
        String input = readLine(prompt + " (y/n): ");
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
    }

    // -------------------------------------------------------------------------
    // Closeable – supports try-with-resources in Main
    // -------------------------------------------------------------------------

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
