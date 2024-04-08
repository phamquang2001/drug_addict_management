package com.system.management.repository;

import com.system.management.model.entity.PoliceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliceRequestRepository extends JpaRepository<PoliceRequest, Long> {

    Optional<PoliceRequest> findByIdAndStatus(Long id, String status);
}