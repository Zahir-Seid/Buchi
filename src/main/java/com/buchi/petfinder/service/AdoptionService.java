package com.buchi.petfinder.service;

import com.buchi.petfinder.dto.AdoptionResponse;
import com.buchi.petfinder.dto.ReportResponse;
import com.buchi.petfinder.entity.Adoption;
import com.buchi.petfinder.entity.Customer;
import com.buchi.petfinder.entity.Pet;
import com.buchi.petfinder.exception.ResourceNotFoundException;
import com.buchi.petfinder.repository.AdoptionRepository;
import com.buchi.petfinder.repository.CustomerRepository;
import com.buchi.petfinder.repository.PetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdoptionService {

    private final AdoptionRepository adoptionRepository;
    private final CustomerRepository customerRepository;
    private final PetRepository petRepository;

    public AdoptionService(AdoptionRepository adoptionRepository,
                           CustomerRepository customerRepository,
                           PetRepository petRepository) {
        this.adoptionRepository = adoptionRepository;
        this.customerRepository = customerRepository;
        this.petRepository = petRepository;
    }

    @Transactional
    public Adoption createAdoption(String customerId, String petId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));

        Adoption adoption = Adoption.builder()
                .customer(customer)
                .pet(pet)
                .build();

        return adoptionRepository.save(adoption);
    }

    @Transactional(readOnly = true)
    public List<AdoptionResponse> getAdoptionRequests(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

        var pageRequest = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt"));
        var adoptions = adoptionRepository.findByDateRangeOrderByDateDesc(fromDateTime, toDateTime, pageRequest).getContent();

        return adoptions.stream()
                .map(this::mapToAdoptionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportResponse generateReport(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

        // Get adopted pet types count
        var petTypeResults = adoptionRepository.countAdoptedPetTypes(fromDateTime, toDateTime);
        Map<String, Long> adoptedPetTypes = new LinkedHashMap<>();
        for (var row : petTypeResults) {
            adoptedPetTypes.put((String) row[0], (Long) row[1]);
        }

        // Get weekly adoption counts
        var weeklyResults = adoptionRepository.countWeeklyAdoptions(fromDateTime, toDateTime);
        Map<String, Long> weeklyAdoptions = new LinkedHashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (var row : weeklyResults) {
            // Convert to LocalDate and format
            Object dateObj = row[0];
            String dateStr;
            if (dateObj instanceof LocalDate ld) {
                dateStr = ld.format(dateFormatter);
            } else if (dateObj instanceof LocalDateTime ldt) {
                dateStr = ldt.toLocalDate().format(dateFormatter);
            } else {
                dateStr = dateObj.toString();
            }
            weeklyAdoptions.put(dateStr, (Long) row[1]);
        }

        return ReportResponse.builder()
                .adoptedPetTypes(adoptedPetTypes)
                .weeklyAdoptionRequests(weeklyAdoptions)
                .build();
    }

    private AdoptionResponse mapToAdoptionResponse(Adoption adoption) {
        return AdoptionResponse.builder()
                .customerId(adoption.getCustomer().getId())
                .customerPhone(adoption.getCustomer().getPhone())
                .customerName(adoption.getCustomer().getName())
                .petId(adoption.getPet().getId())
                .type(adoption.getPet().getType())
                .gender(adoption.getPet().getGender())
                .size(adoption.getPet().getSize())
                .age(adoption.getPet().getAge())
                .goodWithChildren(adoption.getPet().getGoodWithChildren())
                .build();
    }
}
