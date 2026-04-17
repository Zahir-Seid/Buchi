package com.buchi.petfinder.repository;

import com.buchi.petfinder.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    
    Optional<Customer> findByPhone(String phone);
    
    boolean existsByPhone(String phone);
}
