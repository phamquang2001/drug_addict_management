package com.system.management.repository;

import com.system.management.model.entity.TreatmentPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TreatmentPlaceRepository extends JpaRepository<TreatmentPlace, Long> {

    boolean existsByIdAndStatus(Long id, String status);

    Optional<TreatmentPlace> findByIdAndStatus(Long id, String status);
}