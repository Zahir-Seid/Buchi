package com.buchi.petfinder.controller;

import com.buchi.petfinder.dto.*;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.service.PetSearchService;
import com.buchi.petfinder.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@Slf4j
@Tag(name = "Pet", description = "Pet management APIs")
public class PetController {

    private final PetService petService;
    private final PetSearchService petSearchService;

    public PetController(PetService petService, PetSearchService petSearchService) {
        this.petService = petService;
        this.petSearchService = petSearchService;
    }

    @PostMapping(value = "/create_pet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new pet", description = "Creates a new pet and stores it in the local database")
    public ResponseEntity<ApiResponse<CreatePetResponse>> createPet(
            @ModelAttribute @Valid CreatePetRequest request,
            @Parameter(description = "Pet photos")
            @RequestPart(value = "photo", required = false) List<MultipartFile> photos) {

        log.info("Creating new pet: {}", request.getType());

        Pet pet = petService.createPet(request, photos);

        CreatePetResponse response = CreatePetResponse.builder()
                .petId(pet.getId())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/get_pets")
    @Operation(summary = "Search for pets", description = "Search local DB and Dog API for pets")
    public ResponseEntity<ApiResponse<SearchPetsResponse>> getPets(
            @RequestParam(required = false, name = "type") List<String> types,
            @RequestParam(required = false, name = "gender") List<String> genders,
            @RequestParam(required = false, name = "size") List<String> sizes,
            @RequestParam(required = false, name = "age") List<String> ages,
            @RequestParam(required = false, name = "good_with_children") Boolean goodWithChildren,
            @RequestParam(required = false, name = "goodWithChildren") Boolean goodWithChildrenCamelCase,
            @RequestParam(required = true) Integer limit) {

        Boolean resolvedGoodWithChildren = goodWithChildren != null ? goodWithChildren : goodWithChildrenCamelCase;
        log.info("Searching pets with filters: types={}, genders={}, sizes={}, ages={}, goodWithChildren={}, limit={}",
                types, genders, sizes, ages, resolvedGoodWithChildren, limit);

        SearchPetsRequest request = SearchPetsRequest.builder()
                .types(types == null ? List.of() : types)
                .genders(genders == null ? List.of() : genders)
                .sizes(sizes == null ? List.of() : sizes)
                .ages(ages == null ? List.of() : ages)
                .goodWithChildren(resolvedGoodWithChildren)
                .limit(limit)
                .build();

        List<PetResponse> pets = petSearchService.searchPets(request);

        SearchPetsResponse response = SearchPetsResponse.builder()
                .pets(pets)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
