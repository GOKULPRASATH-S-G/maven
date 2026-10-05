package com.hcl.petadoption.util;

import com.hcl.petadoption.model.Adopter;
import com.hcl.petadoption.model.Cat;
import com.hcl.petadoption.model.Dog;
import com.hcl.petadoption.model.Gender;
import com.hcl.petadoption.model.PetStatus;
import com.hcl.petadoption.repository.AdopterRepository;
import com.hcl.petadoption.service.PetService;

/**
 * Loads sample data for demonstration purposes.
 *
 * <p>Provides:
 * <ul>
 *   <li>2 Dogs</li>
 *   <li>2 Cats</li>
 *   <li>Multiple unique traits (using Set semantics in each Pet)</li>
 *   <li>At least one available pet</li>
 *   <li>At least one registered Adopter</li>
 * </ul>
 */
public final class SampleDataLoader {

    private SampleDataLoader() {
        // Utility class – no instances
    }

    /**
     * Loads all sample data into the provided service and repository.
     *
     * @param petService       service to register pets
     * @param adopterRepository repository to save sample adopters
     */
    public static void load(PetService petService, AdopterRepository adopterRepository) {
        loadPets(petService);
        loadAdopters(adopterRepository);
    }

    private static void loadPets(PetService petService) {

        // --- Dog 1: Max – Available ---
        Dog max = new Dog("DOG-001", "Max", 3, "Golden Retriever",
                Gender.MALE, PetStatus.AVAILABLE, false);
        max.addTrait("friendly");
        max.addTrait("vaccinated");
        max.addTrait("house-trained");
        max.addTrait("good-with-children");
        petService.registerPet(max);

        // --- Dog 2: Bella – Available ---
        Dog bella = new Dog("DOG-002", "Bella", 5, "German Shepherd",
                Gender.FEMALE, PetStatus.AVAILABLE, true);
        bella.addTrait("trained");
        bella.addTrait("vaccinated");
        bella.addTrait("guard-dog");
        bella.addTrait("good-with-children");
        petService.registerPet(bella);

        // --- Cat 1: Luna – Available ---
        Cat luna = new Cat("CAT-001", "Luna", 2, "Persian",
                Gender.FEMALE, PetStatus.AVAILABLE, true);
        luna.addTrait("vaccinated");
        luna.addTrait("calm");
        luna.addTrait("indoor-only");
        luna.addTrait("friendly");
        petService.registerPet(luna);

        // --- Cat 2: Oliver – Adopted (demonstrates non-available pet) ---
        Cat oliver = new Cat("CAT-002", "Oliver", 4, "Tabby",
                Gender.MALE, PetStatus.ADOPTED, false);
        oliver.addTrait("independent");
        oliver.addTrait("vaccinated");
        oliver.addTrait("outdoor-friendly");
        petService.registerPet(oliver);
    }

    private static void loadAdopters(AdopterRepository adopterRepository) {
        // Sample adopter 1
        Adopter priya = new Adopter("ADT-SAMPLE1", "Priya Sharma",
                "priya.sharma@example.com", "9876543210");
        adopterRepository.save(priya);

        // Sample adopter 2
        Adopter arjun = new Adopter("ADT-SAMPLE2", "Arjun Mehta",
                "arjun.mehta@example.com", "9123456780");
        adopterRepository.save(arjun);
    }
}
