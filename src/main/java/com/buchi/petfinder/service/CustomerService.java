package com.buchi.petfinder.service;

import com.buchi.petfinder.dto.AddCustomerRequest;
import com.buchi.petfinder.entity.Customer;
import com.buchi.petfinder.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer addCustomer(AddCustomerRequest request) {
        // Check if customer with phone already exists
        return customerRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    Customer customer = Customer.builder()
                            .name(request.getName())
                            .phone(request.getPhone())
                            .build();
                    return customerRepository.save(customer);
                });
    }
}
