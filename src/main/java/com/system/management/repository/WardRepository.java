package com.system.management.repository;

import com.system.management.model.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Long> {

    boolean existsByIdAndStatus(Long id, String status);

    Optional<Ward> findByIdAndStatus(Long id, String status);

    boolean existsByCodeAndStatusAndDistrictIdAndCityId(String code, String status, Long districtId, Long cityId);

    boolean existsByCodeAndStatusAndIdNotAndDistrictIdAndCityId(String code, String status, Long id, Long districtId, Long cityId);
}