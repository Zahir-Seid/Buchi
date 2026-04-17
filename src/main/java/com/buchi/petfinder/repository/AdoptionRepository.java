package com.buchi.petfinder.repository;

import com.buchi.petfinder.entity.Adoption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface AdoptionRepository extends JpaRepository<Adoption, String> {

    @Query("SELECT a FROM Adoption a WHERE a.createdAt >= :fromDate AND a.createdAt <= :toDate ORDER BY a.createdAt ASC")
    List<Adoption> findByDateRange(@Param("fromDate") LocalDateTime fromDate,
                                   @Param("toDate") LocalDateTime toDate);

    @Query("SELECT a FROM Adoption a WHERE a.createdAt >= :fromDate AND a.createdAt <= :toDate ORDER BY a.createdAt DESC")
    Page<Adoption> findByDateRangeOrderByDateDesc(@Param("fromDate") LocalDateTime fromDate,
                                                   @Param("toDate") LocalDateTime toDate,
                                                   Pageable pageable);

    @Query("SELECT p.type, COUNT(a) FROM Adoption a JOIN a.pet p WHERE a.createdAt >= :fromDate AND a.createdAt <= :toDate GROUP BY p.type")
    List<Object[]> countAdoptedPetTypes(@Param("fromDate") LocalDateTime fromDate,
                                        @Param("toDate") LocalDateTime toDate);

    @Query("SELECT FUNCTION('DATE', a.createdAt), COUNT(a) FROM Adoption a WHERE a.createdAt >= :fromDate AND a.createdAt <= :toDate GROUP BY FUNCTION('DATE', a.createdAt) ORDER BY FUNCTION('DATE', a.createdAt)")
    List<Object[]> countWeeklyAdoptions(@Param("fromDate") LocalDateTime fromDate,
                                        @Param("toDate") LocalDateTime toDate);
}
