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

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
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
                            .path("/pets")
                            .queryParam("limit", limit)
                            .queryParam("page", 0)
                            .build())
                    .header("x-api-key", apiProperties.getApiKey())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<PetResponse> results = new ArrayList<>();
            for (JsonNode petNode : extractPetNodes(response)) {
                PetResponse pet = mapDogApiPet(petNode);
                if (pet != null && matchesFilters(pet, request)) {
                    results.add(pet);
                    if (results.size() >= limit) {
                        break;
                    }
                }
            }

            return results;
        } catch (WebClientResponseException e) {
            log.error("Dog API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PetFinderApiException("Failed to fetch from Dog API: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Error calling Dog API", e);
            throw new PetFinderApiException("Failed to call Dog API", e);
        }
    }

    private boolean shouldSearchDogs(SearchPetsRequest request) {
        return request.getTypes().isEmpty()
                || request.getTypes().stream().anyMatch(type -> "dog".equalsIgnoreCase(type));
    }

    private List<JsonNode> extractPetNodes(JsonNode response) {
        if (response == null || response.isNull()) {
            return List.of();
        }
        if (response.isArray()) {
            return toList(response.elements());
        }
        if (response.has("pets") && response.get("pets").isArray()) {
            return toList(response.get("pets").elements());
        }
        if (response.has("data") && response.get("data").isArray()) {
            return toList(response.get("data").elements());
        }
        if (response.has("items") && response.get("items").isArray()) {
            return toList(response.get("items").elements());
        }
        return List.of();
    }

    private List<JsonNode> toList(Iterator<JsonNode> iterator) {
        List<JsonNode> nodes = new ArrayList<>();
        iterator.forEachRemaining(nodes::add);
        return nodes;
    }

    private PetResponse mapDogApiPet(JsonNode petNode) {
        String id = textValue(petNode, "id");
        if (id == null) {
            return null;
        }

        String gender = firstNonBlank(
                textValue(petNode, "gender"),
                textValue(petNode, "gender_prediction"),
                "unknown"
        ).toLowerCase();

        String age = resolveAge(petNode);
        String size = resolveSize(petNode);
        Boolean goodWithChildren = resolveGoodWithChildren(petNode);
        List<String> photos = extractPhotos(petNode);

        return PetResponse.builder()
                .petId("dogapi_" + id)
                .source("dogapi")
                .type("Dog")
                .gender(gender)
                .size(size)
                .age(age)
                .goodWithChildren(goodWithChildren)
                .photos(photos)
                .build();
    }

    private String resolveAge(JsonNode petNode) {
        String age = textValue(petNode, "age");
        if (age != null && !age.isBlank()) {
            return normalizeAge(age);
        }

        String dateOfBirth = textValue(petNode, "date_of_birth");
        if (dateOfBirth == null || dateOfBirth.isBlank()) {
            return "unknown";
        }

        try {
            int years = Period.between(LocalDate.parse(dateOfBirth), LocalDate.now()).getYears();
            if (years <= 0) {
                return "baby";
            }
            if (years <= 2) {
                return "young";
            }
            if (years <= 7) {
                return "adult";
            }
            return "senior";
        } catch (DateTimeParseException ignored) {
            return "unknown";
        }
    }

    private String resolveSize(JsonNode petNode) {
        String size = textValue(petNode, "size");
        if (size != null && !size.isBlank()) {
            return normalizeSize(size);
        }

        String weightKg = textAtPath(petNode, "weight.metric");
        if (weightKg == null) {
            weightKg = textAtPath(petNode, "breed.weight.metric");
        }

        if (weightKg == null) {
            return "unknown";
        }

        double maxWeight = extractMaxNumber(weightKg);
        if (maxWeight <= 0) {
            return "unknown";
        }
        if (maxWeight <= 10) {
            return "small";
        }
        if (maxWeight <= 25) {
            return "medium";
        }
        if (maxWeight <= 40) {
            return "large";
        }
        return "xlarge";
    }

    private Boolean resolveGoodWithChildren(JsonNode petNode) {
        if (petNode.has("good_with_children")) {
            return petNode.get("good_with_children").asBoolean();
        }

        String familyFriendly = firstNonBlank(
                textAtPath(petNode, "breed.family_friendly"),
                textAtPath(petNode, "breeds.0.family_friendly")
        );

        if (familyFriendly == null) {
            return false;
        }

        String normalized = familyFriendly.toLowerCase();
        return !normalized.contains("not recommended") && !normalized.contains("unsupervised");
    }

    private List<String> extractPhotos(JsonNode petNode) {
        List<String> photos = new ArrayList<>();
        if (petNode.has("images") && petNode.get("images").isArray()) {
            for (JsonNode image : petNode.get("images")) {
                String url = textValue(image, "url");
                if (url != null && !url.isBlank()) {
                    photos.add(url);
                }
            }
        }

        String singleImage = textAtPath(petNode, "image.url");
        if (singleImage != null && !singleImage.isBlank()) {
            photos.add(singleImage);
        }

        String directUrl = textValue(petNode, "url");
        if (directUrl != null && !directUrl.isBlank()) {
            photos.add(directUrl);
        }

        return photos.stream().distinct().toList();
    }

    private boolean matchesFilters(PetResponse pet, SearchPetsRequest request) {
        return matches(request.getTypes(), pet.getType())
                && matches(request.getGenders(), pet.getGender())
                && matches(request.getSizes(), pet.getSize())
                && matches(request.getAges(), pet.getAge())
                && (request.getGoodWithChildren() == null
                || request.getGoodWithChildren().equals(pet.getGoodWithChildren()));
    }

    private boolean matches(List<String> filters, String value) {
        return filters.isEmpty() || filters.stream().anyMatch(filter -> filter.equalsIgnoreCase(value));
    }

    private String normalizeAge(String value) {
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "puppy", "newborn", "infant" -> "baby";
            case "junior" -> "young";
            case "mature" -> "adult";
            case "old", "elder", "elderly" -> "senior";
            default -> normalized;
        };
    }

    private String normalizeSize(String value) {
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "extra large", "extra_large", "extra-large", "xl", "x-large" -> "xlarge";
            default -> normalized;
        };
    }

    private double extractMaxNumber(String value) {
        String[] parts = value.split("-");
        String candidate = parts[parts.length - 1].replaceAll("[^0-9.]", "").trim();
        if (candidate.isBlank()) {
            return -1;
        }
        try {
            return Double.parseDouble(candidate);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private String textAtPath(JsonNode node, String path) {
        JsonNode current = node;
        for (String segment : path.split("\\.")) {
            if (current == null || current.isNull()) {
                return null;
            }
            if (segment.matches("\\d+")) {
                if (!current.isArray()) {
                    return null;
                }
                int index = Integer.parseInt(segment);
                if (index >= current.size()) {
                    return null;
                }
                current = current.get(index);
            } else {
                current = current.get(segment);
            }
        }
        if (current == null || current.isNull()) {
            return null;
        }
        return current.asText();
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
