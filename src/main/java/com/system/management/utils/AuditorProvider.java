package com.system.management.utils;

import com.system.management.model.dto.Auditor;
import com.system.management.model.dto.PoliceDto;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorProvider implements AuditorAware<Auditor> {

    private Auditor buildAuditor(PoliceDto police) {
        return police == null ? null : new Auditor(police.getIdentifyNumber(), police.getFullName());
    }

    @Override
    public Optional<Auditor> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication.getDetails() instanceof PoliceDto)
                .map(authentication -> buildAuditor((PoliceDto) authentication.getDetails()));
    }
}
