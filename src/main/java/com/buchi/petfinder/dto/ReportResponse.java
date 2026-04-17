package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    @JsonProperty("adopted_pet_types")
    private Map<String, Long> adoptedPetTypes;

    @JsonProperty("weekly_adoption_requests")
    private Map<String, Long> weeklyAdoptionRequests;
}
