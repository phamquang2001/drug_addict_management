package com.system.management.repository;

import com.system.management.model.entity.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlackListRepository extends JpaRepository<BlackList, Long> {
    boolean existsByToken(String token);
}