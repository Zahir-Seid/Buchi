package com.buchi.petfinder.controller;

import com.buchi.petfinder.dto.*;
import com.buchi.petfinder.entity.Customer;
import com.buchi.petfinder.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/add_customer")
    @Operation(summary = "Add a customer", description = "Creates a new customer or returns existing by phone number")
    public ResponseEntity<ApiResponse<CreateCustomerResponse>> addCustomer(
            @Valid @RequestBody AddCustomerRequest request) {

        log.info("Adding customer with phone: {}", request.getPhone());
        Customer customer = customerService.addCustomer(request);

        CreateCustomerResponse response = CreateCustomerResponse.builder()
                .customerId(customer.getId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
