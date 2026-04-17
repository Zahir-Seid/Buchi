package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdoptionResponse {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("customer_phone")
    private String customerPhone;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("pet_id")
    private String petId;

    private String type;
    private String gender;
    private String size;
    private String age;

    @JsonProperty("good_with_children")
    private Boolean goodWithChildren;
}
