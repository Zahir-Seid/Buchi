package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResponse {

    @JsonProperty("pet_id")
    private String petId;

    private String source;
    private String type;
    private String gender;
    private String size;
    private String age;

    @JsonProperty("good_with_children")
    private Boolean goodWithChildren;

    @JsonProperty("photos")
    private List<String> photos;
}
