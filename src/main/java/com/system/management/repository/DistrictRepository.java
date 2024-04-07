package com.system.management.repository;

import com.system.management.model.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistrictRepository extends JpaRepository<District, Long> {

    boolean existsByIdAndStatus(Long id, String status);

    Optional<District> findByIdAndStatus(Long id, String status);

    boolean existsByCodeAndStatusAndCityId(String code, String status, Long id);

    boolean existsByCodeAndStatusAndIdNotAndCityId(String code, String status, Long id, Long cityId);
}