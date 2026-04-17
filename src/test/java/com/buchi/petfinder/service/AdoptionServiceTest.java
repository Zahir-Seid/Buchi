package com.buchi.petfinder.service;

import com.buchi.petfinder.dto.AdoptionResponse;
import com.buchi.petfinder.entity.Adoption;
import com.buchi.petfinder.entity.Customer;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.exception.ResourceNotFoundException;
import com.buchi.petfinder.repository.AdoptionRepository;
import com.buchi.petfinder.repository.CustomerRepository;
import com.buchi.petfinder.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

    @Mock
    private AdoptionRepository adoptionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private AdoptionService adoptionService;

    private Customer testCustomer;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id("customer-id")
                .name("Test Customer")
                .phone("1234567890")
                .build();

        testPet = Pet.builder()
                .id("pet-id")
                .type("Dog")
                .gender("male")
                .size("small")
                .age("baby")
                .goodWithChildren(true)
                .build();
    }

    @Test
    void createAdoption_success() {
        Adoption adoption = Adoption.builder()
                .id("adoption-id")
                .customer(testCustomer)
                .pet(testPet)
                .build();

        when(customerRepository.findById("customer-id")).thenReturn(Optional.of(testCustomer));
        when(petRepository.findById("pet-id")).thenReturn(Optional.of(testPet));
        when(adoptionRepository.save(any(Adoption.class))).thenReturn(adoption);

        Adoption result = adoptionService.createAdoption("customer-id", "pet-id");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("adoption-id");
        assertThat(result.getCustomer().getId()).isEqualTo("customer-id");
        assertThat(result.getPet().getId()).isEqualTo("pet-id");

        verify(customerRepository).findById("customer-id");
        verify(petRepository).findById("pet-id");
        verify(adoptionRepository).save(any(Adoption.class));
    }

    @Test
    void createAdoption_customerNotFound() {
        when(customerRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adoptionService.createAdoption("invalid-id", "pet-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepository).findById("invalid-id");
        verify(petRepository, never()).findById(any());
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void createAdoption_petNotFound() {
        when(customerRepository.findById("customer-id")).thenReturn(Optional.of(testCustomer));
        when(petRepository.findById("invalid-pet-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adoptionService.createAdoption("customer-id", "invalid-pet-id"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pet not found");

        verify(customerRepository).findById("customer-id");
        verify(petRepository).findById("invalid-pet-id");
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    void getAdoptionRequests_success() {
        Adoption adoption = Adoption.builder()
                .id("adoption-id")
                .customer(testCustomer)
                .pet(testPet)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Adoption> adoptionPage = new PageImpl<>(List.of(adoption));

        when(adoptionRepository.findByDateRangeOrderByDateDesc(any(), any(), any(Pageable.class)))
                .thenReturn(adoptionPage);

        List<AdoptionResponse> result = adoptionService.getAdoptionRequests(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo("customer-id");
        assertThat(result.get(0).getCustomerName()).isEqualTo("Test Customer");
        assertThat(result.get(0).getPetId()).isEqualTo("pet-id");
        assertThat(result.get(0).getType()).isEqualTo("Dog");
    }
}
