package com.system.management.repository;

import com.system.management.model.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WardRepository extends JpaRepository<Ward, Long> {

    Optional<Ward> findByIdAndStatus(Long id, String status);

    boolean existsByCodeAndStatusAndDistrict_Id(String code, String status, Long districtId);

    boolean existsByCodeAndStatusAndIdNotAndDistrict_Id(String code, String status, Long id, Long districtId);
}