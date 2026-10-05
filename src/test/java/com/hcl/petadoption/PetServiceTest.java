package com.hcl.petadoption;

import com.hcl.petadoption.exception.PetNotFoundException;
import com.hcl.petadoption.model.Cat;
import com.hcl.petadoption.model.Dog;
import com.hcl.petadoption.model.Gender;
import com.hcl.petadoption.model.Pet;
import com.hcl.petadoption.model.PetStatus;
import com.hcl.petadoption.repository.PetRepository;
import com.hcl.petadoption.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PetService}.
 *
 * <p>Tests:
 * <ol>
 *   <li>Registering a pet</li>
 *   <li>Finding a pet by ID (Optional)</li>
 *   <li>Pet-not-found behaviour (PetNotFoundException)</li>
 *   <li>Listing and filtering pets</li>
 *   <li>Comparable (natural) sorting</li>
 *   <li>Comparator (custom) sorting</li>
 * </ol>
 */
@DisplayName("PetService Tests")
class PetServiceTest {

    private PetService petService;

    @BeforeEach
    void setUp() {
        petService = new PetService(new PetRepository());
    }

    // -------------------------------------------------------------------------
    // Helper factory
    // -------------------------------------------------------------------------

    private Dog createDog(String id, String name, int age) {
        return new Dog(id, name, age, "Breed-" + id, Gender.MALE, PetStatus.AVAILABLE);
    }

    private Cat createCat(String id, String name, int age) {
        return new Cat(id, name, age, "Breed-" + id, Gender.FEMALE, PetStatus.AVAILABLE);
    }

    // =========================================================================
    // 1. Registering a pet
    // =========================================================================

    @Test
    @DisplayName("Should register a new dog successfully")
    void testRegisterDog() {
        Dog max = createDog("D-001", "Max", 3);
        petService.registerPet(max);

        assertEquals(1, petService.getTotalCount());
        assertTrue(petService.findPetById("D-001").isPresent());
    }

    @Test
    @DisplayName("Should register multiple pets and keep correct count")
    void testRegisterMultiplePets() {
        petService.registerPet(createDog("D-001", "Max", 3));
        petService.registerPet(createCat("C-001", "Luna", 2));
        assertEquals(2, petService.getTotalCount());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for duplicate pet ID")
    void testDuplicatePetIdThrows() {
        petService.registerPet(createDog("D-001", "Max", 3));
        Pet duplicate = createDog("D-001", "Other", 1);
        assertThrows(IllegalArgumentException.class, () -> petService.registerPet(duplicate));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when registering null pet")
    void testRegisterNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> petService.registerPet(null));
    }

    // =========================================================================
    // 2. Finding a pet – Optional behaviour
    // =========================================================================

    @Test
    @DisplayName("findPetById should return Optional with pet when found")
    void testFindPetByIdFound() {
        petService.registerPet(createDog("D-001", "Max", 3));
        Optional<Pet> result = petService.findPetById("D-001");
        assertTrue(result.isPresent());
        assertEquals("Max", result.get().getName());
    }

    @Test
    @DisplayName("findPetById should return Optional.empty for unknown ID")
    void testFindPetByIdNotFound() {
        Optional<Pet> result = petService.findPetById("UNKNOWN");
        assertFalse(result.isPresent());
    }

    // =========================================================================
    // 3. Pet-not-found behaviour (unchecked exception)
    // =========================================================================

    @Test
    @DisplayName("findPetOrThrow should throw PetNotFoundException for unknown ID")
    void testFindPetOrThrow() {
        PetNotFoundException ex = assertThrows(
                PetNotFoundException.class,
                () -> petService.findPetOrThrow("GHOST-ID"));
        assertEquals("GHOST-ID", ex.getPetId());
        assertTrue(ex.getMessage().contains("GHOST-ID"));
    }

    // =========================================================================
    // 4. Listing and filtering pets
    // =========================================================================

    @Test
    @DisplayName("filterByStatus should return only AVAILABLE pets")
    void testFilterByStatus() {
        petService.registerPet(createDog("D-001", "Max", 3));
        Dog adopted = createDog("D-002", "Rex", 5);
        adopted.setStatus(PetStatus.ADOPTED);
        petService.registerPet(adopted);

        List<Pet> available = petService.filterByStatus(PetStatus.AVAILABLE);
        assertEquals(1, available.size());
        assertEquals("Max", available.get(0).getName());
    }

    @Test
    @DisplayName("filterBySpecies should return only Dogs")
    void testFilterBySpeciesDog() {
        petService.registerPet(createDog("D-001", "Max", 3));
        petService.registerPet(createCat("C-001", "Luna", 2));

        List<Dog> dogs = petService.filterBySpecies(Dog.class);
        assertEquals(1, dogs.size());
        assertInstanceOf(Dog.class, dogs.get(0));
    }

    @Test
    @DisplayName("filterBySpecies should return only Cats")
    void testFilterBySpeciesCat() {
        petService.registerPet(createDog("D-001", "Max", 3));
        petService.registerPet(createCat("C-001", "Luna", 2));

        List<Cat> cats = petService.filterBySpecies(Cat.class);
        assertEquals(1, cats.size());
        assertInstanceOf(Cat.class, cats.get(0));
    }

    @Test
    @DisplayName("filterByTraits should only return pets with ALL specified traits")
    void testFilterByTraits() {
        Dog max = createDog("D-001", "Max", 3);
        max.addTrait("vaccinated");
        max.addTrait("friendly");
        petService.registerPet(max);

        Dog rex = createDog("D-002", "Rex", 5);
        rex.addTrait("vaccinated");
        petService.registerPet(rex);

        // Only Max has both traits
        List<Pet> result = petService.filterByTraits(List.of("vaccinated", "friendly"));
        assertEquals(1, result.size());
        assertEquals("Max", result.get(0).getName());
    }

    @Test
    @DisplayName("Traits Set should not allow duplicate entries")
    void testTraitSetUniqueness() {
        Dog max = createDog("D-001", "Max", 3);
        max.addTrait("vaccinated");
        max.addTrait("vaccinated");  // duplicate
        max.addTrait("VACCINATED");  // duplicate after normalisation
        petService.registerPet(max);

        Pet found = petService.findPetOrThrow("D-001");
        assertEquals(1, found.getTraits().size());
    }

    // =========================================================================
    // 5. Comparable (natural) sorting – by name
    // =========================================================================

    @Test
    @DisplayName("listAllPets should return pets sorted by name (Comparable)")
    void testNaturalSortByName() {
        petService.registerPet(createDog("D-001", "Zara", 3));
        petService.registerPet(createDog("D-002", "Apollo", 5));
        petService.registerPet(createCat("C-001", "Max", 2));

        List<Pet> sorted = petService.listAllPets();
        assertEquals("Apollo", sorted.get(0).getName());
        assertEquals("Max",    sorted.get(1).getName());
        assertEquals("Zara",   sorted.get(2).getName());
    }

    // =========================================================================
    // 6. Comparator (custom) sorting
    // =========================================================================

    @Test
    @DisplayName("sortedBy BY_AGE_ASC should return youngest first (Comparator)")
    void testSortByAgeAscending() {
        petService.registerPet(createDog("D-001", "Old Rex", 7));
        petService.registerPet(createDog("D-002", "Young Max", 1));
        petService.registerPet(createCat("C-001", "Mid Luna", 4));

        List<Pet> sorted = petService.sortedBy(PetService.BY_AGE_ASC);
        assertEquals(1, sorted.get(0).getAge());
        assertEquals(4, sorted.get(1).getAge());
        assertEquals(7, sorted.get(2).getAge());
    }

    @Test
    @DisplayName("sortedBy BY_AGE_DESC should return oldest first (Comparator)")
    void testSortByAgeDescending() {
        petService.registerPet(createDog("D-001", "Old Rex", 7));
        petService.registerPet(createDog("D-002", "Young Max", 1));

        List<Pet> sorted = petService.sortedBy(PetService.BY_AGE_DESC);
        assertEquals(7, sorted.get(0).getAge());
        assertEquals(1, sorted.get(1).getAge());
    }

    @Test
    @DisplayName("sortedBy BY_BREED should return pets ordered alphabetically by breed")
    void testSortByBreed() {
        Dog a = new Dog("D-001", "X", 2, "Beagle", Gender.MALE, PetStatus.AVAILABLE);
        Dog b = new Dog("D-002", "Y", 3, "Akita",  Gender.MALE, PetStatus.AVAILABLE);
        petService.registerPet(a);
        petService.registerPet(b);

        List<Pet> sorted = petService.sortedBy(PetService.BY_BREED);
        assertEquals("Akita",  sorted.get(0).getBreed());
        assertEquals("Beagle", sorted.get(1).getBreed());
    }

    // =========================================================================
    // Polymorphism
    // =========================================================================

    @Test
    @DisplayName("getSpeciesInfo should return different text for Dog vs Cat (polymorphism)")
    void testPolymorphism() {
        Dog dog = createDog("D-001", "Max", 3);
        Cat cat = createCat("C-001", "Luna", 2);
        assertNotEquals(dog.getSpeciesInfo(), cat.getSpeciesInfo());
        assertNotEquals(dog.makeSound(), cat.makeSound());
    }
}
