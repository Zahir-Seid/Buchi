package com.buchi.petfinder.service;

import com.buchi.petfinder.config.FileStorageProperties;
import com.buchi.petfinder.dto.CreatePetRequest;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.repository.PetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PetService {

    private final PetRepository petRepository;
    private final FileStorageProperties fileStorageProperties;

    public PetService(PetRepository petRepository, FileStorageProperties fileStorageProperties) {
        this.petRepository = petRepository;
        this.fileStorageProperties = fileStorageProperties;
    }

    @Transactional
    public Pet createPet(CreatePetRequest request, List<MultipartFile> photos) {
        List<String> photoUrls = new ArrayList<>();

        if (photos != null && !photos.isEmpty()) {
            photoUrls = savePhotos(photos);
        }

        Pet pet = Pet.builder()
                .type(request.getType())
                .gender(request.getGender())
                .size(request.getSize())
                .age(request.getAge())
                .goodWithChildren(request.getGoodWithChildren())
                .photos(photoUrls)
                .build();

        return petRepository.save(pet);
    }

    private List<String> savePhotos(List<MultipartFile> photos) {
        List<String> urls = new ArrayList<>();

        try {
            Path uploadPath = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            for (MultipartFile photo : photos) {
                String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                Path targetLocation = uploadPath.resolve(fileName);
                Files.copy(photo.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                urls.add("/uploads/" + fileName);
            }
        } catch (IOException e) {
            log.error("Failed to store photos", e);
            throw new RuntimeException("Failed to store photos", e);
        }

        return urls;
    }
}
