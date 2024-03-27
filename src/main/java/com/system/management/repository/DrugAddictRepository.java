package com.system.management.repository;

import com.system.management.model.entity.DrugAddict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrugAddictRepository extends JpaRepository<DrugAddict, Long> {

    Optional<DrugAddict> findByIdAndStatus(Long id, String status);
}