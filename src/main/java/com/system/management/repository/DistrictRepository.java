package com.system.management.repository;

import com.system.management.model.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {

    Optional<District> findByIdAndStatus(Long id, String status);

    boolean existsByCodeAndStatusAndCity_Id(String code, String status, Long id);

    boolean existsByCodeAndStatusAndIdNotAndCity_Id(String code, String status, Long id, Long cityId);
}