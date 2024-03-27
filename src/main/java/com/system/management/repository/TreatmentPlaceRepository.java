package com.system.management.repository;

import com.system.management.model.entity.TreatmentPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TreatmentPlaceRepository extends JpaRepository<TreatmentPlace, Long> {

    Optional<TreatmentPlace> findByIdAndStatus(Long id, String status);
}