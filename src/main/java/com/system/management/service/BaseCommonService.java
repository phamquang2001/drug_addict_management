package com.system.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.system.management.model.dto.*;
import com.system.management.model.entity.*;
import com.system.management.repository.CityRepository;
import com.system.management.repository.DistrictRepository;
import com.system.management.repository.PoliceRepository;
import com.system.management.repository.WardRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.AssignStatusEnums;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
public class BaseCommonService {

    @Autowired
    protected Gson gson;

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

    protected PoliceDto convertToPoliceDto(Police police) {

        PoliceDto policeDto = modelMapper.map(police, PoliceDto.class);
        policeDto.setRoleName(RoleEnums.dict.get(police.getRole()).label);
        policeDto.setLevelName(LevelEnums.dict.get(police.getLevel()).label);
        policeDto.setAssignStatusName(AssignStatusEnums.dict.get(police.getAssignStatus()).label);
        setAuditorInfo(police, policeDto);
        setCadastralInfo(policeDto);

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

        return policeDto;
    }

    protected PoliceRequestDto convertToPoliceRequestDto(PoliceRequest policeRequest) {
        PoliceRequestDto policeRequestDto = modelMapper.map(policeRequest, PoliceRequestDto.class);
        policeRequestDto.setRoleName(RoleEnums.dict.get(policeRequest.getRole()).label);
        policeRequestDto.setLevelName(LevelEnums.dict.get(policeRequest.getLevel()).label);
        setAuditorInfo(policeRequest, policeRequestDto);
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
        setAuditorInfo(drugAddict, drugAddictDto);

        if (drugAddictDto.getAvatar() != null) {
            drugAddictDto.setStrAvatar(Base64.getEncoder().encodeToString(drugAddictDto.getAvatar()));
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getPoliceId())) {
            drugAddictDto.setPolice(findPoliceByIdWithoutAuditor(drugAddict.getPoliceId()));
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getTreatmentPlaceId())) {
            drugAddictDto.setTreatmentPlace(findTreatmentPlaceByIdWithoutAuditor(drugAddict.getTreatmentPlaceId()));
        }

        String fullPermanent = "";

        if (StringUtils.isNotBlank(drugAddictDto.getPermanentAddressDetail())) {
            fullPermanent = drugAddictDto.getPermanentAddressDetail() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getPermanentWardId())) {
            drugAddictDto.setPermanentWard(findWardByIdWithoutAuditor(drugAddict.getPermanentWardId()));
            fullPermanent = fullPermanent + drugAddictDto.getPermanentWard().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getPermanentDistrictId())) {
            drugAddictDto.setPermanentDistrict(findDistrictByIdWithoutAuditor(drugAddict.getPermanentDistrictId()));
            fullPermanent = fullPermanent + drugAddictDto.getPermanentDistrict().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getPermanentCityId())) {
            drugAddictDto.setPermanentCity(findCityByIdWithoutAuditor(drugAddict.getPermanentCityId()));
            fullPermanent = fullPermanent + drugAddictDto.getPermanentCity().getFullName();
        }

        drugAddictDto.setFullPermanent(fullPermanent);

        String fullCurrent = "";

        if (StringUtils.isNotBlank(drugAddictDto.getCurrentAddressDetail())) {
            fullCurrent = drugAddictDto.getCurrentAddressDetail() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getCurrentWardId())) {
            drugAddictDto.setCurrentWard(findWardByIdWithoutAuditor(drugAddict.getCurrentWardId()));
            fullCurrent = fullCurrent + drugAddictDto.getCurrentWard().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getCurrentDistrictId())) {
            drugAddictDto.setCurrentDistrict(findDistrictByIdWithoutAuditor(drugAddict.getCurrentDistrictId()));
            fullCurrent = fullCurrent + drugAddictDto.getCurrentDistrict().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddict.getCurrentCityId())) {
            drugAddictDto.setCurrentCity(findCityByIdWithoutAuditor(drugAddict.getCurrentCityId()));
            fullCurrent = fullCurrent + drugAddictDto.getCurrentCity().getFullName();
        }

        drugAddictDto.setFullCurrent(fullCurrent);

        return drugAddictDto;
    }

    protected DrugAddictRequestDto convertToDrugAddictRequestDto(DrugAddictRequest drugAddictRequest) {

        DrugAddictRequestDto drugAddictRequestDto = modelMapper.map(drugAddictRequest, DrugAddictRequestDto.class);
        drugAddictRequestDto.setPermanentCity(findCityByIdWithoutAuditor(drugAddictRequest.getPermanentCityId()));
        drugAddictRequestDto.setPermanentDistrict(findDistrictByIdWithoutAuditor(drugAddictRequest.getPermanentDistrictId()));
        drugAddictRequestDto.setPermanentWard(findWardByIdWithoutAuditor(drugAddictRequest.getPermanentWardId()));
        drugAddictRequestDto.setCurrentCity(findCityByIdWithoutAuditor(drugAddictRequest.getCurrentCityId()));
        drugAddictRequestDto.setCurrentDistrict(findDistrictByIdWithoutAuditor(drugAddictRequest.getCurrentDistrictId()));
        drugAddictRequestDto.setCurrentWard(findWardByIdWithoutAuditor(drugAddictRequest.getCurrentWardId()));
        setAuditorInfo(drugAddictRequest, drugAddictRequestDto);

        if (drugAddictRequestDto.getAvatar() != null) {
            drugAddictRequestDto.setStrAvatar(Base64.getEncoder().encodeToString(drugAddictRequestDto.getAvatar()));
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getPoliceId())) {
            drugAddictRequestDto.setPolice(findPoliceByIdWithoutAuditor(drugAddictRequest.getPoliceId()));
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getTreatmentPlaceId())) {
            drugAddictRequestDto.setTreatmentPlace(findTreatmentPlaceByIdWithoutAuditor(drugAddictRequest.getTreatmentPlaceId()));
        }

        String fullPermanent = "";

        if (StringUtils.isNotBlank(drugAddictRequestDto.getPermanentAddressDetail())) {
            fullPermanent = drugAddictRequestDto.getPermanentAddressDetail() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getPermanentWardId())) {
            drugAddictRequestDto.setPermanentWard(findWardByIdWithoutAuditor(drugAddictRequest.getPermanentWardId()));
            fullPermanent = fullPermanent + drugAddictRequestDto.getPermanentWard().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getPermanentDistrictId())) {
            drugAddictRequestDto.setPermanentDistrict(findDistrictByIdWithoutAuditor(drugAddictRequest.getPermanentDistrictId()));
            fullPermanent = fullPermanent + drugAddictRequestDto.getPermanentDistrict().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getPermanentCityId())) {
            drugAddictRequestDto.setPermanentCity(findCityByIdWithoutAuditor(drugAddictRequest.getPermanentCityId()));
            fullPermanent = fullPermanent + drugAddictRequestDto.getPermanentCity().getFullName();
        }

        drugAddictRequestDto.setFullPermanent(fullPermanent);

        String fullCurrent = "";

        if (StringUtils.isNotBlank(drugAddictRequestDto.getCurrentAddressDetail())) {
            fullCurrent = drugAddictRequestDto.getCurrentAddressDetail() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getCurrentWardId())) {
            drugAddictRequestDto.setCurrentWard(findWardByIdWithoutAuditor(drugAddictRequest.getCurrentWardId()));
            fullCurrent = fullCurrent + drugAddictRequestDto.getCurrentWard().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getCurrentDistrictId())) {
            drugAddictRequestDto.setCurrentDistrict(findDistrictByIdWithoutAuditor(drugAddictRequest.getCurrentDistrictId()));
            fullCurrent = fullCurrent + drugAddictRequestDto.getCurrentDistrict().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(drugAddictRequest.getCurrentCityId())) {
            drugAddictRequestDto.setCurrentCity(findCityByIdWithoutAuditor(drugAddictRequest.getCurrentCityId()));
            fullCurrent = fullCurrent + drugAddictRequestDto.getCurrentCity().getFullName();
        }

        drugAddictRequestDto.setFullCurrent(fullCurrent);

        return drugAddictRequestDto;
    }

    protected TreatmentPlaceDto convertToTreatmentPlaceDto(TreatmentPlace treatmentPlace) {

        TreatmentPlaceDto treatmentPlaceDto = modelMapper.map(treatmentPlace, TreatmentPlaceDto.class);
        setAuditorInfo(treatmentPlace, treatmentPlaceDto);
        setCadastralInfo(treatmentPlaceDto);

        String fullAddress = "";

        if (StringUtils.isNotBlank(treatmentPlace.getAddressDetail())) {
            fullAddress = treatmentPlace.getAddressDetail() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(treatmentPlace.getWardId())) {
            fullAddress = fullAddress + treatmentPlaceDto.getWard().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(treatmentPlace.getDistrictId())) {
            fullAddress = fullAddress + treatmentPlaceDto.getDistrict().getFullName() + ", ";
        }

        if (!FunctionUtils.isNullOrZero(treatmentPlace.getCityId())) {
            fullAddress = fullAddress + treatmentPlaceDto.getCity().getFullName();
        }

        treatmentPlaceDto.setFullAddress(fullAddress);

        return treatmentPlaceDto;
    }

    protected AssignSupportDto convertToAssignSupportDto(AssignSupport assignSupport) {

        AssignSupportDto assignSupportDto = modelMapper.map(assignSupport, AssignSupportDto.class);
        setAuditorInfo(assignSupport, assignSupportDto);
        setCadastralInfo(assignSupportDto);

        if (!FunctionUtils.isNullOrZero(assignSupport.getPoliceId())) {
            assignSupportDto.setPolice(findPoliceByIdWithoutAuditor(assignSupport.getPoliceId()));
        }

        if (!FunctionUtils.isNullOrZero(assignSupport.getDrugAddictId())) {
            DrugAddict drugAddict = findDrugAddictByIdWithoutAuditor(assignSupport.getDrugAddictId());
            assignSupportDto.setDrugAddict(convertToDrugAddictDto(drugAddict));
        }

        if (!FunctionUtils.isNullOrZero(assignSupport.getLevel())) {
            assignSupportDto.setLevelName(LevelEnums.dict.get(assignSupport.getLevel()).label);
        }

        return assignSupportDto;
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

    private void setAuditorInfo(BaseEntity entity, BaseDto dto) {

        if (StringUtils.isNotBlank(entity.getTxtCreatedBy())) {
            dto.setCreatedBy(gson.fromJson(entity.getTxtCreatedBy(), Auditor.class));
        }

        if (StringUtils.isNotBlank(entity.getTxtModifiedBy())) {
            dto.setModifiedBy(gson.fromJson(entity.getTxtModifiedBy(), Auditor.class));
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

        PoliceDto policeDto = namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(PoliceDto.class));

        if (policeDto != null) {
            policeDto.setLevelName(LevelEnums.dict.get(policeDto.getLevel()).label);
        }

        return policeDto;
    }

    protected DrugAddict findDrugAddictByIdWithoutAuditor(Long id) {
        String sql = "select id, " +
                "            identify_number, " +
                "            full_name, " +
                "            permanent_city_id, " +
                "            permanent_district_id, " +
                "            permanent_ward_id, " +
                "            permanent_address_detail " +
                "     from drug_addicts where id = :id";
        sqlParameterSource.addValue("id", id);
        return namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(DrugAddict.class));
    }

    protected TreatmentPlaceDto findTreatmentPlaceByIdWithoutAuditor(Long id) {
        String sql = "select id, full_name from treatment_places where id = :id";
        sqlParameterSource.addValue("id", id);
        return namedParameterJdbcTemplate
                .queryForObject(sql, sqlParameterSource, BeanPropertyRowMapper.newInstance(TreatmentPlaceDto.class));
    }
}
