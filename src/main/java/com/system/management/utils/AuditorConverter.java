package com.system.management.utils;

import com.google.gson.Gson;
import com.system.management.model.dto.Auditor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Converter;

@Slf4j
@Convert
@Converter
@RequiredArgsConstructor
public class AuditorConverter implements AttributeConverter<Auditor, String> {

    private final Gson gson;

    @Override
    public String convertToDatabaseColumn(Auditor auditor) {
        return auditor == null ? gson.toJson(new Auditor("Hệ thống")) : gson.toJson(auditor);
    }

    @Override
    public Auditor convertToEntityAttribute(String str) {
        return StringUtils.isBlank(str) ? null : gson.fromJson(str, Auditor.class);
    }
}
