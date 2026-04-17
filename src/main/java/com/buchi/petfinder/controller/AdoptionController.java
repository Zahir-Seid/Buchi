package com.buchi.petfinder.controller;

import com.buchi.petfinder.dto.*;
import com.buchi.petfinder.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "Adoption", description = "Adoption management APIs")
public class AdoptionController {

    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @PostMapping("/adopt")
    @Operation(summary = "Create adoption request", description = "Creates an adoption record for a customer and pet")
    public ResponseEntity<ApiResponse<CreateAdoptionResponse>> adopt(@Valid @RequestBody AdoptRequest request) {

        log.info("Creating adoption request for customer: {}, pet: {}", request.getCustomerId(), request.getPetId());
        var adoption = adoptionService.createAdoption(request.getCustomerId(), request.getPetId());

        CreateAdoptionResponse response = CreateAdoptionResponse.builder()
                .adoptionId(adoption.getId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/get_adoption_requests")
    @Operation(summary = "Get adoption requests", description = "Fetch adoption requests in a date range")
    public ResponseEntity<ApiResponse<List<AdoptionResponse>>> getAdoptionRequests(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from_date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to_date) {

        log.info("Fetching adoption requests from {} to {}", from_date, to_date);
        List<AdoptionResponse> requests = adoptionService.getAdoptionRequests(from_date, to_date);

        return ResponseEntity.ok(ApiResponse.success(requests));
    }
}
