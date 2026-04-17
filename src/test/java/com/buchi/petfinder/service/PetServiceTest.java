package com.buchi.petfinder.service;

import com.buchi.petfinder.config.FileStorageProperties;
import com.buchi.petfinder.dto.CreatePetRequest;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetService petService;

    private FileStorageProperties fileStorageProperties;

    @BeforeEach
    void setUp() {
        fileStorageProperties = new FileStorageProperties();
        fileStorageProperties.setUploadDir("./uploads/test-pets");
        ReflectionTestUtils.setField(petService, "fileStorageProperties", fileStorageProperties);
    }

    @Test
    void createPet_withoutPhotos() {
        CreatePetRequest request = CreatePetRequest.builder()
                .type("Dog")
                .gender("male")
                .size("small")
                .age("baby")
                .goodWithChildren(true)
                .build();

        Pet savedPet = Pet.builder()
                .id("test-pet-id")
                .type("Dog")
                .gender("male")
                .size("small")
                .age("baby")
                .goodWithChildren(true)
                .photos(List.of())
                .build();

        when(petRepository.save(any(Pet.class))).thenReturn(savedPet);

        Pet result = petService.createPet(request, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-pet-id");
        assertThat(result.getType()).isEqualTo("Dog");
        assertThat(result.getGoodWithChildren()).isTrue();

        verify(petRepository).save(argThat(pet ->
                pet.getType().equals("Dog") &&
                pet.getGender().equals("male") &&
                pet.getPhotos().isEmpty()
        ));
    }

    @Test
    void createPet_withPhotos() {
        CreatePetRequest request = CreatePetRequest.builder()
                .type("Cat")
                .gender("female")
                .size("medium")
                .age("young")
                .goodWithChildren(false)
                .build();

        MockMultipartFile photo = new MockMultipartFile(
                "photo", "cat.jpg", "image/jpeg", "test image content".getBytes()
        );

        Pet savedPet = Pet.builder()
                .id("test-pet-id-2")
                .type("Cat")
                .gender("female")
                .size("medium")
                .age("young")
                .goodWithChildren(false)
                .photos(List.of("/uploads/test-filename.jpg"))
                .build();

        when(petRepository.save(any(Pet.class))).thenReturn(savedPet);

        Pet result = petService.createPet(request, List.of(photo));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("test-pet-id-2");
        assertThat(result.getType()).isEqualTo("Cat");

        verify(petRepository).save(argThat(pet ->
                pet.getType().equals("Cat") &&
                !pet.getPhotos().isEmpty()
        ));
    }
}
