package com.system.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.management.model.dto.*;
import com.system.management.model.entity.*;
import com.system.management.repository.CityRepository;
import com.system.management.repository.DistrictRepository;
import com.system.management.repository.PoliceRepository;
import com.system.management.repository.WardRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.constants.ErrorMessage;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Objects;

@Slf4j
@Service
public class BaseCommonService {

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected FunctionUtils functionUtils;

    @Autowired
    protected CityRepository cityRepository;

    @Autowired
    protected WardRepository wardRepository;

    @Autowired
    protected PoliceRepository policeRepository;

    @Autowired
    protected DistrictRepository districtRepository;

    @Autowired
    protected MapSqlParameterSource sqlParameterSource;

    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected PoliceDto getLoggedAccount() {
        return (PoliceDto) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }

    protected void isAllow(Long wardId, Long districtId, Long cityId) {

        PoliceDto loggedAccount = getLoggedAccount();

        if (Objects.equals(loggedAccount.getLevel(), LevelEnums.CENTRAL.value)
                && Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            return;
        }

        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new BadRequestException(ErrorMessage.NOT_ALLOW);
        }

        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value
                && !Objects.equals(loggedAccount.getCity().getId(), cityId)) {
            throw new BadRequestException(ErrorMessage.NOT_ALLOW);
        }

        if (loggedAccount.getLevel() > LevelEnums.CITY.value
                && !Objects.equals(loggedAccount.getDistrict().getId(), districtId)) {
            throw new BadRequestException(ErrorMessage.NOT_ALLOW);
        }

        if (loggedAccount.getLevel() > LevelEnums.DISTRICT.value
                && !Objects.equals(loggedAccount.getWard().getId(), wardId)) {
            throw new BadRequestException(ErrorMessage.NOT_ALLOW);
        }
    }

    protected PoliceDto convertToPoliceDto(Police police) {
        PoliceDto policeDto = modelMapper.map(police, PoliceDto.class);
        policeDto.setRoleName(RoleEnums.dict.get(police.getRole()).label);
        policeDto.setLevelName(LevelEnums.dict.get(police.getLevel()).label);

        if (police.getAvatar() != null) {
            policeDto.setStrAvatar(Base64.getEncoder().encodeToString(police.getAvatar()));
        }

        String workPlace = "";

        if (police.getLevel() > LevelEnums.CENTRAL.value) {
            workPlace = policeDto.getCity().getFullName() + workPlace;
        }

        if (police.getLevel() > LevelEnums.CITY.value) {
            workPlace = policeDto.getDistrict().getFullName() + ", " + workPlace;
        }

        if (police.getLevel() > LevelEnums.DISTRICT.value) {
            workPlace = policeDto.getWard().getFullName() + ", " + workPlace;
        }

        policeDto.setWorkPlace(workPlace);

        setCadastralInfo(policeDto);
        return policeDto;
    }

    protected PoliceRequestDto convertToPoliceRequestDto(PoliceRequest policeRequest) {
        PoliceRequestDto policeRequestDto = modelMapper.map(policeRequest, PoliceRequestDto.class);
        policeRequestDto.setRoleName(RoleEnums.dict.get(policeRequest.getRole()).label);
        policeRequestDto.setLevelName(LevelEnums.dict.get(policeRequest.getLevel()).label);
        setCadastralInfo(policeRequestDto);

        if (policeRequestDto.getAvatar() != null) {
            policeRequestDto.setStrAvatar(Base64.getEncoder().encodeToString(policeRequestDto.getAvatar()));
        }

        String workPlace = "";

        if (policeRequest.getLevel() > LevelEnums.CENTRAL.value) {
            workPlace = policeRequestDto.getCity().getFullName() + workPlace;
        }

        if (policeRequest.getLevel() > LevelEnums.CITY.value) {
            workPlace = policeRequestDto.getDistrict().getFullName() + ", " + workPlace;
        }

        if (policeRequest.getLevel() > LevelEnums.DISTRICT.value) {
            workPlace = policeRequestDto.getWard().getFullName() + ", " + workPlace;
        }

        policeRequestDto.setWorkPlace(workPlace);

        return policeRequestDto;
    }

    protected DrugAddictDto convertToDrugAddictDto(DrugAddict drugAddict) {
        DrugAddictDto drugAddictDto = modelMapper.map(drugAddict, DrugAddictDto.class);
        drugAddictDto.setPolice(findPoliceByIdWithoutAuditor(drugAddict.getPoliceId()));
        drugAddictDto.setTreatmentPlace(findTreatmentPlaceByIdWithoutAuditor(drugAddict.getTreatmentPlaceId()));
        drugAddictDto.setPermanentCity(findCityByIdWithoutAuditor(drugAddict.getPermanentCityId()));
        drugAddictDto.setPermanentDistrict(findDistrictByIdWithoutAuditor(drugAddict.getPermanentDistrictId()));
        drugAddictDto.setPermanentWard(findWardByIdWithoutAuditor(drugAddict.getPermanentWardId()));
        drugAddictDto.setCurrentCity(findCityByIdWithoutAuditor(drugAddict.getCurrentCityId()));
        drugAddictDto.setCurrentDistrict(findDistrictByIdWithoutAuditor(drugAddict.getCurrentDistrictId()));
        drugAddictDto.setCurrentWard(findWardByIdWithoutAuditor(drugAddict.getCurrentWardId()));

        if (drugAddictDto.getAvatar() != null) {
            drugAddictDto.setStrAvatar(Base64.getEncoder().encodeToString(drugAddictDto.getAvatar()));
        }

        String fullPermanent = drugAddictDto.getPermanentAddressDetail().trim() + ", "
                + drugAddictDto.getPermanentWard().getFullName() + ", "
                + drugAddictDto.getPermanentDistrict().getFullName() + ", "
                + drugAddictDto.getPermanentCity().getFullName();
        drugAddictDto.setFullPermanent(fullPermanent);

        String fullCurrent = drugAddictDto.getCurrentAddressDetail().trim() + ", "
                + drugAddictDto.getCurrentWard().getFullName() + ", "
                + drugAddictDto.getCurrentDistrict().getFullName() + ", "
                + drugAddictDto.getCurrentCity().getFullName();
        drugAddictDto.setFullCurrent(fullCurrent);

        return drugAddictDto;
    }

    protected DrugAddictRequestDto convertToDrugAddictRequestDto(DrugAddictRequest drugAddictRequest) {
        DrugAddictRequestDto drugAddictRequestDto = modelMapper.map(drugAddictRequest, DrugAddictRequestDto.class);
        drugAddictRequestDto.setPolice(findPoliceByIdWithoutAuditor(drugAddictRequest.getPoliceId()));
        drugAddictRequestDto.setTreatmentPlace(findTreatmentPlaceByIdWithoutAuditor(drugAddictRequest.getTreatmentPlaceId()));
        drugAddictRequestDto.setPermanentCity(findCityByIdWithoutAuditor(drugAddictRequest.getPermanentCityId()));
        drugAddictRequestDto.setPermanentDistrict(findDistrictByIdWithoutAuditor(drugAddictRequest.getPermanentDistrictId()));
        drugAddictRequestDto.setPermanentWard(findWardByIdWithoutAuditor(drugAddictRequest.getPermanentWardId()));
        drugAddictRequestDto.setCurrentCity(findCityByIdWithoutAuditor(drugAddictRequest.getCurrentCityId()));
        drugAddictRequestDto.setCurrentDistrict(findDistrictByIdWithoutAuditor(drugAddictRequest.getCurrentDistrictId()));
        drugAddictRequestDto.setCurrentWard(findWardByIdWithoutAuditor(drugAddictRequest.getCurrentWardId()));

        if (drugAddictRequestDto.getAvatar() != null) {
            drugAddictRequestDto.setStrAvatar(Base64.getEncoder().encodeToString(drugAddictRequestDto.getAvatar()));
        }

        String fullPermanent = drugAddictRequestDto.getPermanentAddressDetail().trim() + ", "
                + drugAddictRequestDto.getPermanentWard().getFullName() + ", "
                + drugAddictRequestDto.getPermanentDistrict().getFullName() + ", "
                + drugAddictRequestDto.getPermanentCity().getFullName();
        drugAddictRequestDto.setFullPermanent(fullPermanent);

        String fullCurrent = drugAddictRequestDto.getCurrentAddressDetail().trim() + ", "
                + drugAddictRequestDto.getCurrentWard().getFullName() + ", "
                + drugAddictRequestDto.getCurrentDistrict().getFullName() + ", "
                + drugAddictRequestDto.getCurrentCity().getFullName();
        drugAddictRequestDto.setFullCurrent(fullCurrent);

        return drugAddictRequestDto;
    }

    protected TreatmentPlaceDto convertToTreatmentPlaceDto(TreatmentPlace treatmentPlace) {
        TreatmentPlaceDto treatmentPlaceDto = modelMapper.map(treatmentPlace, TreatmentPlaceDto.class);
        setCadastralInfo(treatmentPlaceDto);

        String workPlace = treatmentPlaceDto.getCity().getFullName() + ", "
                + treatmentPlaceDto.getDistrict().getFullName() + ", "
                + treatmentPlaceDto.getWard().getFullName();
        treatmentPlaceDto.setFullAddress(workPlace);

        return treatmentPlaceDto;
    }

    protected void setCadastralInfo(BaseCadastralDto dto) {
        if (!FunctionUtils.isNullOrZero(dto.getCityId())) {
            dto.setCity(findCityByIdWithoutAuditor(dto.getCityId()));
        }

        if (!FunctionUtils.isNullOrZero(dto.getDistrictId())) {
            dto.setDistrict(findDistrictByIdWithoutAuditor(dto.getDistrictId()));
        }

        if (!FunctionUtils.isNullOrZero(dto.getWardId())) {
            dto.setWard(findWardByIdWithoutAuditor(dto.getWardId()));
        }
    }

    protected CityDto findCityByIdWithoutAuditor(Long id) {
        String sql = "select id, code, full_name, status from cities where id = :id";
        sqlParameterSource.addValue("id", id);
        return namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(CityDto.class));
    }

    public DistrictDto findDistrictByIdWithoutAuditor(Long id) {
        String sql = "select id, code, full_name, status from districts where id = :id";
        sqlParameterSource.addValue("id", id);
        return namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(DistrictDto.class));
    }

    public WardDto findWardByIdWithoutAuditor(Long id) {
        String sql = "select id, code, full_name, status from wards where id = :id";
        sqlParameterSource.addValue("id", id);
        return namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(WardDto.class));
    }

    protected PoliceDto findPoliceByIdWithoutAuditor(Long id) {
        String sql = "select id, identify_number, full_name, level from polices where id = :id";
        sqlParameterSource.addValue("id", id);
        return namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(PoliceDto.class));
    }

    protected TreatmentPlaceDto findTreatmentPlaceByIdWithoutAuditor(Long id) {
        String sql = "select id, full_name from treatment_places where id = :id";
        sqlParameterSource.addValue("id", id);
        return namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(TreatmentPlaceDto.class));
    }
}
