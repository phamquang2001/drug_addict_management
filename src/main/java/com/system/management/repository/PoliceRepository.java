package com.system.management.repository;

import com.system.management.model.entity.Police;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PoliceRepository extends JpaRepository<Police, Long> {

    boolean existsByIdAndStatus(Long id, String status);

    Optional<Police> findByIdAndStatus(Long id, String status);

    boolean existsByIdentifyNumberAndStatus(String identifyNumber, String status);

    Optional<Police> findByIdentifyNumberAndStatus(String identifyNumber, String status);

    @Modifying
    @Query(value = "update polices set role = :role where id = :id", nativeQuery = true)
    void updateRoleOldSheriff(@Param(value = "role") Integer role, @Param(value = "id") Long id);
}