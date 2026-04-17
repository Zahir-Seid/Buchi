package com.buchi.petfinder.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateRangeRequest {

    @NotNull(message = "from_date is required")
    @JsonAlias("fromDate")
    private LocalDate fromDate;

    @NotNull(message = "to_date is required")
    @JsonAlias("toDate")
    private LocalDate toDate;
}
