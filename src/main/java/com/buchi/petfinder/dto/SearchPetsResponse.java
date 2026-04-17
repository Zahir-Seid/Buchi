package com.buchi.petfinder.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchPetsResponse {

    private List<PetResponse> pets;
}
