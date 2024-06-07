package com.system.management.repository;

import com.system.management.model.entity.AssignSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AssignSupportRepository extends JpaRepository<AssignSupport, Long> {

    Optional<AssignSupport> findByIdAndStatus(Long id, String status);

    Optional<AssignSupport> findByDrugAddictIdAndStatus(Long drugAddictId, String status);

    @Transactional
    void deleteAllByPoliceIdAndDrugAddictId(Long policeId, Long drugAddictId);

    boolean existsByPoliceIdAndStatus(Long policeId, String status);

    boolean existsByPoliceIdAndDrugAddictIdAndStatus(Long policeId, Long drugAddictId, String status);

    boolean existsByPoliceIdAndCityIdAndDistrictIdAndWardIdAndStatus(Long policeId, Long cityId, Long districtId, Long wardId, String status);
}