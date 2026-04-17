package com.buchi.petfinder.service;

import com.buchi.petfinder.dto.AddCustomerRequest;
import com.buchi.petfinder.entity.Customer;
import com.buchi.petfinder.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void addCustomer_newCustomer() {
        AddCustomerRequest request = AddCustomerRequest.builder()
                .name("John Doe")
                .phone("1234567890")
                .build();

        Customer newCustomer = Customer.builder()
                .id("new-customer-id")
                .name("John Doe")
                .phone("1234567890")
                .build();

        when(customerRepository.findByPhone("1234567890")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(newCustomer);

        Customer result = customerService.addCustomer(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("new-customer-id");
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getPhone()).isEqualTo("1234567890");

        verify(customerRepository).findByPhone("1234567890");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void addCustomer_existingCustomer() {
        AddCustomerRequest request = AddCustomerRequest.builder()
                .name("Jane Doe")
                .phone("0987654321")
                .build();

        Customer existingCustomer = Customer.builder()
                .id("existing-customer-id")
                .name("Jane Doe Old")
                .phone("0987654321")
                .build();

        when(customerRepository.findByPhone("0987654321")).thenReturn(Optional.of(existingCustomer));

        Customer result = customerService.addCustomer(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("existing-customer-id");
        assertThat(result.getPhone()).isEqualTo("0987654321");

        verify(customerRepository).findByPhone("0987654321");
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
