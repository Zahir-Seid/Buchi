package com.buchi.petfinder.service;

import com.buchi.petfinder.client.DogApiClient;
import com.buchi.petfinder.dto.PetResponse;
import com.buchi.petfinder.dto.SearchPetsRequest;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.repository.PetRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PetSearchService {

    private final PetRepository petRepository;
    private final DogApiClient dogApiClient;

    public PetSearchService(PetRepository petRepository, DogApiClient dogApiClient) {
        this.petRepository = petRepository;
        this.dogApiClient = dogApiClient;
    }

    @Transactional(readOnly = true)
    public List<PetResponse> searchPets(SearchPetsRequest request) {
        int limit = request.getLimit() != null ? request.getLimit() : 10;

        // Search local database first
        Specification<Pet> spec = buildSpecification(request);
        List<Pet> localPets = petRepository.findAll(spec);

        // Map local pets to response
        List<PetResponse> results = new ArrayList<>(localPets.stream()
                .map(this::mapToPetResponse)
                .toList());

        // If we need more results, fetch from Dog API.
        if (results.size() < limit) {
            int externalLimit = limit - results.size();
            try {
                results.addAll(dogApiClient.searchPets(request, externalLimit));
            } catch (Exception e) {
                log.warn("Failed to fetch from Dog API", e);
            }
        }

        // Ensure we don't exceed the limit
        return results.stream().limit(limit).toList();
    }

    private Specification<Pet> buildSpecification(SearchPetsRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            addMultiValuePredicate(predicates, cb, root.get("type"), request.getTypes());
            addMultiValuePredicate(predicates, cb, root.get("gender"), request.getGenders());
            addMultiValuePredicate(predicates, cb, root.get("size"), request.getSizes());
            addMultiValuePredicate(predicates, cb, root.get("age"), request.getAges());
            if (request.getGoodWithChildren() != null) {
                predicates.add(cb.equal(root.get("goodWithChildren"), request.getGoodWithChildren()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PetResponse mapToPetResponse(Pet pet) {
        return PetResponse.builder()
                .petId(pet.getId())
                .source("local")
                .type(pet.getType())
                .gender(pet.getGender())
                .size(pet.getSize())
                .age(pet.getAge())
                .goodWithChildren(pet.getGoodWithChildren())
                .photos(pet.getPhotos())
                .build();
    }

    private void addMultiValuePredicate(List<Predicate> predicates,
                                        jakarta.persistence.criteria.CriteriaBuilder cb,
                                        jakarta.persistence.criteria.Path<String> field,
                                        List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        var inClause = cb.in(cb.lower(field));
        values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::toLowerCase)
                .forEach(inClause::value);
        predicates.add(inClause);
    }
}
