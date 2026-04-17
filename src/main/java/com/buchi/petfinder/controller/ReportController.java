package com.buchi.petfinder.controller;

import com.buchi.petfinder.dto.ApiResponse;
import com.buchi.petfinder.dto.DateRangeRequest;
import com.buchi.petfinder.dto.ReportResponse;
import com.buchi.petfinder.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "Report", description = "Report generation APIs")
public class ReportController {

    private final AdoptionService adoptionService;

    public ReportController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @PostMapping("/generate_report")
    @Operation(summary = "Generate adoption report", description = "Generate a report with adoption statistics for a date range")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @Valid @RequestBody DateRangeRequest request) {

        log.info("Generating report from {} to {}", request.getFromDate(), request.getToDate());
        ReportResponse report = adoptionService.generateReport(request.getFromDate(), request.getToDate());

        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
