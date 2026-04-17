package com.buchi.petfinder.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchPetsRequest {

    @Builder.Default
    private List<String> types = List.of();

    @Builder.Default
    private List<String> genders = List.of();

    @Builder.Default
    private List<String> sizes = List.of();

    @Builder.Default
    private List<String> ages = List.of();

    private Boolean goodWithChildren;
    private Integer limit;
}
