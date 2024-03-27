package com.system.management.repository;

import com.system.management.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByPoliceId(Long policeId);

    Optional<RefreshToken> findByToken(String token);

    void deleteAllByPoliceId(Long policeId);
}