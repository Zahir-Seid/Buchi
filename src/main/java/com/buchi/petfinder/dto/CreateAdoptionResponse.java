package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdoptionResponse {

    @JsonProperty("adoption_id")
    private String adoptionId;
}
