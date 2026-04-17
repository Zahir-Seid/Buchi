package com.buchi.petfinder.controller;

import com.buchi.petfinder.entity.Adoption;
import com.buchi.petfinder.entity.Customer;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.repository.AdoptionRepository;
import com.buchi.petfinder.repository.CustomerRepository;
import com.buchi.petfinder.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerAndAdoptionControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private AdoptionRepository adoptionRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        adoptionRepository.deleteAll();
        customerRepository.deleteAll();
        petRepository.deleteAll();
    }

    @Test
    void addCustomer_newCustomer() throws Exception {
        String customerJson = """
                {
                    "name": "Abebe Kebede",
                    "phone": "0922222222"
                }
                """;

                mockMvc.perform(post("/api/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.customer_id").isNotEmpty());
    }

    @Test
    void addCustomer_duplicatePhone() throws Exception {
        String customerJson = """
                {
                    "name": "First Customer",
                    "phone": "0911111111"
                }
                """;

        // Add first customer
        String response = mockMvc.perform(post("/api/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Try to add another customer with same phone
        String duplicateJson = """
                {
                    "name": "Second Customer",
                    "phone": "0911111111"
                }
                """;

                mockMvc.perform(post("/api/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.customer_id").isNotEmpty());
    }

    @Test
    void addCustomer_missingFields() throws Exception {
        String customerJson = """
                {
                    "name": "Test"
                }
                """;

        mockMvc.perform(post("/api/add_customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adopt_success() throws Exception {
        // Create customer and pet first
        Customer customer = customerRepository.save(Customer.builder()
                .name("Test Customer")
                .phone("0933333333")
                .build());

        Pet pet = petRepository.save(Pet.builder()
                .type("Dog")
                .gender("male")
                .size("small")
                .age("baby")
                .goodWithChildren(true)
                .build());

        String adoptJson = """
                {
                    "customerId": "%s",
                    "petId": "%s"
                }
                """.formatted(customer.getId(), pet.getId());

                mockMvc.perform(post("/api/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adoptJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.adoption_id").isNotEmpty());
    }

    @Test
    void adopt_invalidCustomerId() throws Exception {
        String adoptJson = """
                {
                    "customerId": "invalid-id",
                    "petId": "some-pet-id"
                }
                """;

        mockMvc.perform(post("/api/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adoptJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void adopt_invalidPetId() throws Exception {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Test Customer")
                .phone("0944444444")
                .build());

        String adoptJson = """
                {
                    "customerId": "%s",
                    "petId": "invalid-pet-id"
                }
                """.formatted(customer.getId());

        mockMvc.perform(post("/api/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adoptJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAdoptionRequests_success() throws Exception {
        // Create test data
        Customer customer = customerRepository.save(Customer.builder()
                .name("Test Customer")
                .phone("0955555555")
                .build());

        Pet pet = petRepository.save(Pet.builder()
                .type("Cat")
                .gender("female")
                .size("medium")
                .age("adult")
                .goodWithChildren(false)
                .build());

        adoptionRepository.save(Adoption.builder()
                .customer(customer)
                .pet(pet)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/get_adoption_requests")
                        .param("from_date", "2020-01-01")
                        .param("to_date", "2030-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].customer_name").value("Test Customer"))
                .andExpect(jsonPath("$.data[0].type").value("Cat"));
    }
}
