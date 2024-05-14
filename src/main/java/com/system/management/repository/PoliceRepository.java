package com.system.management.repository;

import com.system.management.model.entity.Police;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoliceRepository extends JpaRepository<Police, Long> {

    boolean existsByIdAndStatus(Long id, String status);

    Optional<Police> findByIdAndStatus(Long id, String status);

    boolean existsByIdentifyNumberAndStatus(String identifyNumber, String status);

    Optional<Police> findByIdentifyNumberAndStatus(String identifyNumber, String status);
}