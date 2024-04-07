package com.system.management.repository;

import com.system.management.model.entity.PoliceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoliceRequestRepository extends JpaRepository<PoliceRequest, Long> {
}