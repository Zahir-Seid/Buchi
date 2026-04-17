package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePetResponse {

    @JsonProperty("pet_id")
    private String petId;
}
