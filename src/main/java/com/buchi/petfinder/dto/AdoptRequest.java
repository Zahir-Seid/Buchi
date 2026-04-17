package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptRequest {

    @NotBlank(message = "Customer ID is required")
    @JsonAlias("customer_id")
    private String customerId;

    @NotBlank(message = "Pet ID is required")
    @JsonAlias("pet_id")
    private String petId;
}
