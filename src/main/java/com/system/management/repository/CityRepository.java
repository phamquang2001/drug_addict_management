package com.system.management.repository;

import com.system.management.model.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByCode(String code);

    Optional<City> findByIdAndStatus(Long id, String status);

    boolean existsByCodeAndStatus(String code, String status);

    boolean existsByCodeAndStatusAndIdNot(String code, String status, Long id);
}