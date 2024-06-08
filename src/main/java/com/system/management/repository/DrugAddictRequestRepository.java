package com.system.management.repository;

import com.system.management.model.entity.DrugAddictRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DrugAddictRequestRepository extends JpaRepository<DrugAddictRequest, Long> {

    Optional<DrugAddictRequest> findByIdAndStatus(Long id, String status);

    Optional<DrugAddictRequest> findByDrugAddictIdAndStatus(Long drugAddictId, String status);
}