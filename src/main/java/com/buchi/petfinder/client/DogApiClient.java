package com.buchi.petfinder.client;

import com.buchi.petfinder.config.DogApiProperties;
import com.buchi.petfinder.dto.PetResponse;
import com.buchi.petfinder.dto.SearchPetsRequest;
import com.buchi.petfinder.exception.PetFinderApiException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DogApiClient {

    private final WebClient webClient;
    private final DogApiProperties apiProperties;

    public DogApiClient(WebClient.Builder webClientBuilder, DogApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getBaseUrl()).build();
        this.apiProperties = apiProperties;
    }

    public List<PetResponse> searchPets(SearchPetsRequest request, int limit) {

        if (apiProperties.getApiKey() == null || apiProperties.getApiKey().isBlank()) {
            log.warn("Dog API key not configured, skipping external search");
            return List.of();
        }

        if (!shouldSearchDogs(request)) {
            return List.of();
        }

        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/images/search")
                            .queryParam("limit", limit)
                            .queryParam("has_breeds", 1)
                            .build())
                    .header("x-api-key", apiProperties.getApiKey())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<PetResponse> results = new ArrayList<>();

            if (response != null && response.isArray()) {
                for (JsonNode node : response) {

                    PetResponse pet = mapDogApiPet(node);

                    if (pet != null && matchesFiltersLoosely(pet, request)) {
                        results.add(pet);
                        if (results.size() >= limit) break;
                    }
                }
            }

            log.info("Dog API returned {} pets", results.size());
            return results;

        } catch (WebClientResponseException e) {
            log.error("Dog API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PetFinderApiException("Dog API error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Error calling Dog API", e);
            throw new PetFinderApiException("Failed to call Dog API", e);
        }
    }

    private boolean shouldSearchDogs(SearchPetsRequest request) {
        return request.getTypes().isEmpty()
                || request.getTypes().stream()
                .anyMatch(type -> "dog".equalsIgnoreCase(type));
    }

    private PetResponse mapDogApiPet(JsonNode node) {

        String id = text(node, "id");
        if (id == null) return null;

        JsonNode breed = node.path("breeds").isArray() && node.path("breeds").size() > 0
                ? node.path("breeds").get(0)
                : null;

        String size = resolveSizeFromBreed(breed);

        return PetResponse.builder()
                .petId("dogapi_" + id)
                .source("dogapi")
                .type("Dog")
                .gender("unknown")
                .size(size)
                .age("unknown")
                .goodWithChildren(null)
                .photos(extractPhotos(node))
                .build();
    }

    private List<String> extractPhotos(JsonNode node) {
        List<String> photos = new ArrayList<>();

        String url = text(node, "url");
        if (url != null && !url.isBlank()) {
            photos.add(url);
        }

        return photos;
    }

    private String resolveSizeFromBreed(JsonNode breed) {
        if (breed == null) return "unknown";

        String weight = textAtPath(breed, "weight.metric");
        if (weight == null) return "unknown";

        double max = extractMax(weight);

        if (max <= 10) return "small";
        if (max <= 25) return "medium";
        if (max <= 40) return "large";
        return "xlarge";
    }

    private boolean matchesFiltersLoosely(PetResponse pet, SearchPetsRequest request) {
        return matches(request.getTypes(), pet.getType())
                && matchesNullable(request.getGenders(), pet.getGender())
                && matchesNullable(request.getSizes(), pet.getSize())
                && matchesNullable(request.getAges(), pet.getAge())
                && (request.getGoodWithChildren() == null
                || pet.getGoodWithChildren() == null
                || request.getGoodWithChildren().equals(pet.getGoodWithChildren()));
    }

    private boolean matches(List<String> filters, String value) {
        return filters.isEmpty()
                || filters.stream().anyMatch(f -> f.equalsIgnoreCase(value));
    }

    private boolean matchesNullable(List<String> filters, String value) {
        return filters.isEmpty()
                || value == null
                || filters.stream().anyMatch(f -> f.equalsIgnoreCase(value));
    }

    private String text(JsonNode node, String field) {
        JsonNode val = node.get(field);
        return (val == null || val.isNull()) ? null : val.asText();
    }

    private String textAtPath(JsonNode node, String path) {
        JsonNode current = node;

        for (String part : path.split("\\.")) {
            if (current == null || current.isNull()) return null;

            if (part.matches("\\d+")) {
                if (!current.isArray()) return null;
                current = current.get(Integer.parseInt(part));
            } else {
                current = current.get(part);
            }
        }

        return (current == null || current.isNull()) ? null : current.asText();
    }

    private double extractMax(String value) {
        try {
            String[] parts = value.split("-");
            return Double.parseDouble(parts[parts.length - 1].replaceAll("[^0-9.]", "").trim());
        } catch (Exception e) {
            return -1;
        }
    }
}