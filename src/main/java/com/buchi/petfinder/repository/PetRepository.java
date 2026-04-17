package com.buchi.petfinder.repository;

import com.buchi.petfinder.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, String>, JpaSpecificationExecutor<Pet> {
    
    List<Pet> findTop5ByOrderByCreatedAtDesc();
}
