package com.buchi.petfinder.service;

import com.buchi.petfinder.client.DogApiClient;
import com.buchi.petfinder.dto.PetResponse;
import com.buchi.petfinder.dto.SearchPetsRequest;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.repository.PetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetSearchServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private DogApiClient dogApiClient;

    @InjectMocks
    private PetSearchService petSearchService;

    @Test
    void searchPets_returnsLocalResultsFirstAndBackfillsFromDogApi() {
        SearchPetsRequest request = SearchPetsRequest.builder()
                .types(List.of("Dog"))
                .genders(List.of("male"))
                .limit(2)
                .build();

        Pet localPet = Pet.builder()
                .id("local-1")
                .type("Dog")
                .gender("male")
                .size("small")
                .age("baby")
                .goodWithChildren(true)
                .photos(List.of("/uploads/local.jpg"))
                .build();

        PetResponse externalPet = PetResponse.builder()
                .petId("dogapi_1")
                .source("dogapi")
                .type("Dog")
                .gender("male")
                .size("medium")
                .age("young")
                .goodWithChildren(true)
                .photos(List.of("https://example.com/dog.jpg"))
                .build();

        when(petRepository.findAll(any(Specification.class))).thenReturn(List.of(localPet));
        when(dogApiClient.searchPets(any(SearchPetsRequest.class), eq(1))).thenReturn(List.of(externalPet));

        List<PetResponse> results = petSearchService.searchPets(request);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getSource()).isEqualTo("local");
        assertThat(results.get(0).getPetId()).isEqualTo("local-1");
        assertThat(results.get(1).getSource()).isEqualTo("dogapi");

        verify(dogApiClient).searchPets(request, 1);
    }
}
