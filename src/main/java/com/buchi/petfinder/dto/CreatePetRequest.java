package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePetRequest {

    @NotBlank(message = "Pet type is required")
    private String type;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Size is required")
    private String size;

    @NotBlank(message = "Age is required")
    private String age;

    @NotNull(message = "goodWithChildren is required")
    @JsonAlias("good_with_children")
    private Boolean goodWithChildren;
}
