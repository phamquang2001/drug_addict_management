package com.system.management.repository;

import com.system.management.model.entity.Police;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliceRepository extends JpaRepository<Police, Long> {

    boolean existsByIdentifyNumberAndStatus(String identifyNumber, String status);

    Optional<Police> findByIdentifyNumber(String identifyNumber);

    Optional<Police> findByIdAndStatus(Long id, String status);
}