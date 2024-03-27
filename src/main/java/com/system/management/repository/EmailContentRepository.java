package com.system.management.repository;

import com.system.management.model.entity.EmailContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailContentRepository extends JpaRepository<EmailContent, Long> {

    EmailContent findByType(String type);
}