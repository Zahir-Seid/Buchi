package com.buchi.petfinder.controller;

import com.buchi.petfinder.client.DogApiClient;
import com.buchi.petfinder.dto.PetResponse;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class PetControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private DogApiClient dogApiClient;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        petRepository.deleteAll();
        when(dogApiClient.searchPets(any(), anyInt())).thenReturn(List.of());
    }

    @Test
    void createPet_success() throws Exception {
        String petJson = """
                {
                    "type": "Dog",
                    "gender": "male",
                    "size": "small",
                    "age": "baby",
                    "goodWithChildren": true
                }
                """;

        MockMultipartFile data = new MockMultipartFile(
                "data", "", "application/json", petJson.getBytes()
        );

        mockMvc.perform(multipart("/api/create_pet")
                        .file(data)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.pet_id").isNotEmpty());
    }

    @Test
    void createPet_missingFields() throws Exception {
        String petJson = """
                {
                    "type": "Dog"
                }
                """;

        MockMultipartFile data = new MockMultipartFile(
                "data", "", "application/json", petJson.getBytes()
        );

        mockMvc.perform(multipart("/api/create_pet")
                        .file(data)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPets_success() throws Exception {
        // Create a pet first
        Pet pet = Pet.builder()
                .type("Dog")
                .gender("male")
                .size("small")
                .age("baby")
                .goodWithChildren(true)
                .photos(List.of("/uploads/photo1.jpg"))
                .build();
        petRepository.save(pet);

        mockMvc.perform(get("/api/get_pets")
                        .param("type", "Dog")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.pets").isArray())
                .andExpect(jsonPath("$.data.pets[0].source").value("local"))
                .andExpect(jsonPath("$.data.pets[0].type").value("Dog"));
    }

    @Test
    void getPets_missingLimit() throws Exception {
        mockMvc.perform(get("/api/get_pets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPets_withDogApiResults() throws Exception {
        List<PetResponse> dogApiPets = List.of(
                PetResponse.builder()
                        .petId("dogapi_123")
                        .source("dogapi")
                        .type("Dog")
                        .gender("female")
                        .size("medium")
                        .age("young")
                        .goodWithChildren(true)
                        .photos(List.of("http://example.com/dog.jpg"))
                        .build()
        );

        when(dogApiClient.searchPets(any(), anyInt())).thenReturn(dogApiPets);

        mockMvc.perform(get("/api/get_pets")
                        .param("type", "Dog")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.pets[0].source").value("dogapi"))
                .andExpect(jsonPath("$.data.pets[0].pet_id").value("dogapi_123"));
    }

    @Test
    void getPets_withMultiSelectFilters() throws Exception {
        petRepository.save(Pet.builder()
                .type("Cat")
                .gender("female")
                .size("medium")
                .age("young")
                .goodWithChildren(true)
                .build());

        petRepository.save(Pet.builder()
                .type("Dog")
                .gender("male")
                .size("small")
                .age("baby")
                .goodWithChildren(true)
                .build());

        mockMvc.perform(get("/api/get_pets")
                        .param("type", "Dog", "Cat")
                        .param("gender", "female", "male")
                        .param("age", "young")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pets.length()").value(1))
                .andExpect(jsonPath("$.data.pets[0].type").value("Cat"))
                .andExpect(jsonPath("$.data.pets[0].gender").value("female"));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        DogApiClient dogApiClient() {
            return Mockito.mock(DogApiClient.class);
        }
    }
}
