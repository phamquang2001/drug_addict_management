package com.system.management.service;

import com.system.management.model.dto.*;
import com.system.management.model.entity.*;
import com.system.management.model.request.assign_support.*;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.AssignSupportRepository;
import com.system.management.repository.DrugAddictRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.enums.StatusEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ForbiddenException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.AssignStatusEnums.ASSIGNED;
import static com.system.management.utils.enums.AssignStatusEnums.UN_ASSIGN;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignSupportService extends BaseCommonService {

    private final DrugAddictRepository drugAddictRepository;

    private final AssignSupportRepository assignSupportRepository;

    public SuccessResponse<Object> isAssigned(Long drugAddictId) {

        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(drugAddictId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

        AssignSupport assignSupport = assignSupportRepository
                .findByDrugAddictIdAndStatus(drugAddict.getId(), ACTIVE.name()).orElse(null);
        if (assignSupport == null) {
            return new SuccessResponse<>();
        }

        Police police = policeRepository
                .findByIdAndStatus(assignSupport.getPoliceId(), StatusEnums.ACTIVE.name()).orElse(null);
        if (police == null) {
            assignSupport.setStatus(DELETED.name());
            assignSupportRepository.save(assignSupport);
            return new SuccessResponse<>();
        }

        String error = IS_ASSIGNED.replace("$[0]", drugAddict.getFullName())
                .replace("$[1]", drugAddict.getIdentifyNumber())
                .replace("$[2]", police.getFullName())
                .replace("$[3]", police.getIdentifyNumber());
        throw new BadRequestException(error);
    }


    public SuccessResponse<Object> assignDrugAddict(AssignDrugAddictRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Long policeId = request.getPoliceId();
        Long drugAddictId = request.getDrugAddictId();
        if (assignSupportRepository.existsByPoliceIdAndDrugAddictIdAndStatus(policeId, drugAddictId, ACTIVE.name())) {
            throw new BadRequestException(ALREADY_ASSIGNED_DRUG_ADDICT);
        }

        Police police = policeRepository
                .findByIdAndStatus(policeId, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(drugAddictId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

        assignSupportRepository.deleteAllByPoliceIdAndDrugAddictId(drugAddict.getPoliceId(), drugAddict.getId());

        drugAddict.setPoliceId(police.getId());
        drugAddictRepository.save(drugAddict);

        AssignSupport assignSupport = new AssignSupport();
        assignSupport.setPoliceId(police.getId());
        assignSupport.setDrugAddictId(drugAddict.getId());
        assignSupport.setStatus(ACTIVE.name());
        assignSupportRepository.save(assignSupport);

        police.setAssignStatus(ASSIGNED.getValue());
        policeRepository.save(police);

        return new SuccessResponse<>(convertToAssignSupportDto(assignSupport));
    }

    public SuccessResponse<Object> assignCadastral(AssignCadastralRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Long policeId = request.getPoliceId();
        Integer levelValue = request.getLevel();
        Long cityId = request.getCityId();
        Long districtId = request.getDistrictId();
        Long wardId = request.getWardId();

        if (assignSupportRepository.existsByPoliceIdAndCityIdAndDistrictIdAndWardIdAndStatus(
                policeId, cityId, districtId, wardId, ACTIVE.name())) {
            throw new BadRequestException(ALREADY_ASSIGNED_CADASTRAL);
        }

        Police police = policeRepository
                .findByIdAndStatus(policeId, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        if (Objects.equals(cityId, police.getCityId())
                && Objects.equals(districtId, police.getCityId())
                && Objects.equals(wardId, police.getWardId())) {
            throw new BadRequestException(NOT_ALLOW_ASSIGNED_CADASTRAL);
        }

        if (levelValue > LevelEnums.CENTRAL.value
                && (FunctionUtils.isNullOrZero(cityId) || !cityRepository.existsByIdAndStatus(cityId, ACTIVE.name()))) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        if (levelValue > LevelEnums.CITY.value
                && (FunctionUtils.isNullOrZero(districtId) || !districtRepository.existsByIdAndStatus(districtId, ACTIVE.name()))) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        if (levelValue > LevelEnums.DISTRICT.value
                && (FunctionUtils.isNullOrZero(wardId) || !wardRepository.existsByIdAndStatus(wardId, ACTIVE.name()))) {
            throw new BadRequestException(WARD_NOT_EXISTS);
        }

        LevelEnums level = LevelEnums.dict.get(levelValue);
        if (level == null) {
            throw new BadRequestException(INVALID_LEVEL);
        }

        AssignSupport assignSupport = new AssignSupport();
        assignSupport.setPoliceId(police.getId());
        assignSupport.setCityId(cityId);
        assignSupport.setDistrictId(districtId);
        assignSupport.setWardId(wardId);
        assignSupport.setLevel(level.value);
        assignSupport.setStatus(ACTIVE.name());
        assignSupportRepository.save(assignSupport);

        police.setAssignStatus(ASSIGNED.getValue());
        policeRepository.save(police);

        return new SuccessResponse<>(convertToAssignSupportDto(assignSupport));
    }

    public SuccessResponse<Object> delete(Long id) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        AssignSupport assignSupport = assignSupportRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ASSIGN_SUPPORT_NOT_EXISTS));

        assignSupport.setStatus(DELETED.name());
        assignSupportRepository.save(assignSupport);

        if (!assignSupportRepository.existsByPoliceIdAndStatus(assignSupport.getPoliceId(), ACTIVE.name())) {

            Police police = policeRepository.findById(assignSupport.getPoliceId()).orElse(null);

            if (police != null) {
                police.setAssignStatus(UN_ASSIGN.getValue());
                policeRepository.save(police);
            }
        }

        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getListAssignedDrugAddict(GetListAssignedDrugAddictRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        StringBuilder sql = new StringBuilder();

        sql.append(" select a.id                       as id,");
        sql.append("        b.identify_number          as da_identify_number,");
        sql.append("        b.full_name                as da_full_name,");
        sql.append("        b.permanent_ward_id        as da_permanent_ward_id,");
        sql.append("        b.permanent_district_id    as da_permanent_district_id,");
        sql.append("        b.permanent_city_id        as da_permanent_city_id,");
        sql.append("        b.permanent_address_detail as da_permanent_address_detail,");
        sql.append("        a.police_id                as police_id,");
        sql.append("        a.drug_addict_id           as drug_addict_id,");
        sql.append("        a.created_at               as created_at,");
        sql.append("        a.created_by               as txt_created_by");
        sql.append(" from assign_supports a join drug_addicts b on a.drug_addict_id = b.id");
        sql.append(" where a.police_id = :police_id and a.drug_addict_id is not null");

        sqlParameterSource.addValue("police_id", request.getPoliceId());

        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {
            sql.append(" and b.identify_number like concat('%', :identify_number, '%') ");
            sqlParameterSource.addValue("identify_number", request.getIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and b.full_name like concat('%', :full_name, '%') ");
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and b.permanent_city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and b.permanent_district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and b.permanent_ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        if (request.getStartDate() != null) {
            sql.append(" and DATE (a.created_at) >= DATE (:start_date) ");
            sqlParameterSource.addValue("start_date", request.getStartDate());
        }

        if (request.getEndDate() != null) {
            sql.append(" and DATE (a.created_at) <= DATE (:end_date) ");
            sqlParameterSource.addValue("end_date", request.getEndDate());
        }

        sql.append(" and a.status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        sql.append(" order by a.created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<AssignSupport> assignSupports = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(AssignSupport.class));

        List<AssignSupportDto> assignSupportDtos = new ArrayList<>();

        assignSupports.forEach(item -> assignSupportDtos.add(convertToAssignSupportDto(item)));

        return new SuccessResponse<>(assignSupportDtos);
    }

    public SuccessResponse<Object> getListUnassignedDrugAddict(GetListUnassignedDrugAddictRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        StringBuilder sql = new StringBuilder();

        sql.append(" select id                       as id,");
        sql.append("        identify_number          as identify_number,");
        sql.append("        full_name                as full_name,");
        sql.append("        permanent_ward_id        as permanent_ward_id,");
        sql.append("        permanent_district_id    as permanent_district_id,");
        sql.append("        permanent_city_id        as permanent_city_id,");
        sql.append("        permanent_address_detail as permanent_address_detail");
        sql.append(" from drug_addicts where (police_id is null or police_id <> :police_id)");

        sqlParameterSource.addValue("police_id", request.getPoliceId());

        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {
            sql.append(" and identify_number like concat('%', :identify_number, '%') ");
            sqlParameterSource.addValue("identify_number", request.getIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and full_name like concat('%', :full_name, '%') ");
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and permanent_city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and permanent_district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and permanent_ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<DrugAddict> drugAddicts = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(DrugAddict.class));

        List<DrugAddictDto> drugAddictDtos = new ArrayList<>();

        drugAddicts.forEach(item -> drugAddictDtos.add(convertToDrugAddictDto(item)));

        return new SuccessResponse<>(drugAddictDtos);
    }

    public SuccessResponse<Object> getListAssignedCadastral(GetListAssignCadastralRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        StringBuilder sql = new StringBuilder();

        sql.append(" select id, city_id, district_id, ward_id, level, created_at, created_by as txt_created_by from assign_supports ");
        sql.append(" where police_id = :police_id and city_id is not null ");

        sqlParameterSource.addValue("police_id", request.getPoliceId());

        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        if (request.getStartDate() != null) {
            sql.append(" and DATE (created_at) >= DATE (:start_date) ");
            sqlParameterSource.addValue("start_date", request.getStartDate());
        }

        if (request.getEndDate() != null) {
            sql.append(" and DATE (created_at) <= DATE (:end_date) ");
            sqlParameterSource.addValue("end_date", request.getEndDate());
        }

        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<AssignSupport> assignSupports = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(AssignSupport.class));

        List<AssignSupportDto> assignSupportDtos = new ArrayList<>();

        assignSupports.forEach(item -> assignSupportDtos.add(convertToAssignSupportDto(item)));

        return new SuccessResponse<>(assignSupportDtos);
    }

    public SuccessResponse<Object> getListUnassignedCadastral(GetListUnassignedCadastralRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (Objects.equals(request.getLevel(), LevelEnums.CITY.value)) {
            return getListUnassignedCity(request);
        } else if (Objects.equals(request.getLevel(), LevelEnums.DISTRICT.value)) {
            return getListUnassignedDistrict(request);
        } else if (Objects.equals(request.getLevel(), LevelEnums.WARD.value)) {
            return getListUnassignedWard(request);
        }

        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getListUnassignedCity(GetListUnassignedCadastralRequest request) {

        StringBuilder sql = new StringBuilder();

        sql.append(" select city_id from assign_supports where police_id = :police_id and level = :level ");
        sqlParameterSource.addValue("police_id", request.getPoliceId());
        sqlParameterSource.addValue("level", LevelEnums.CITY.value);

        List<Integer> ids = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(Integer.class));

        sql = new StringBuilder();

        sql.append(" select id, code, full_name from cities where status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        if (!ids.isEmpty()) {
            sql.append(" and id not in (:ids) ");
            sqlParameterSource.addValue("ids", ids);
        }

        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and id = :id ");
            sqlParameterSource.addValue("id", request.getCityId());
        }

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<City> cities = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(City.class));

        List<CityDto> cityDtos = new ArrayList<>();

        cities.forEach(city -> cityDtos.add(modelMapper.map(city, CityDto.class)));

        return new SuccessResponse<>(cityDtos);
    }

    public SuccessResponse<Object> getListUnassignedDistrict(GetListUnassignedCadastralRequest request) {

        StringBuilder sql = new StringBuilder();

        sql.append(" select district_id from assign_supports where police_id = :police_id and level = :level ");
        sqlParameterSource.addValue("police_id", request.getPoliceId());
        sqlParameterSource.addValue("level", LevelEnums.DISTRICT.value);

        List<Integer> ids = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(Integer.class));

        sql = new StringBuilder();

        sql.append(" select id, code, full_name, city_id from districts where status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        if (!ids.isEmpty()) {
            sql.append(" and id not in (:ids) ");
            sqlParameterSource.addValue("ids", ids);
        }

        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and id = :id ");
            sqlParameterSource.addValue("id", request.getDistrictId());
        }

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<District> districts = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(District.class));

        List<DistrictDto> districtDtos = new ArrayList<>();

        districts.forEach(district -> {
            DistrictDto districtDto = modelMapper.map(district, DistrictDto.class);
            districtDto.setCity(findCityByIdWithoutAuditor(districtDto.getCityId()));
            districtDtos.add(districtDto);

        });

        return new SuccessResponse<>(districtDtos);
    }

    public SuccessResponse<Object> getListUnassignedWard(GetListUnassignedCadastralRequest request) {

        StringBuilder sql = new StringBuilder();

        sql.append(" select ward_id from assign_supports where police_id = :police_id and level = :level ");
        sqlParameterSource.addValue("police_id", request.getPoliceId());
        sqlParameterSource.addValue("level", LevelEnums.WARD.value);

        List<Integer> ids = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, new SingleColumnRowMapper<>(Integer.class));

        sql = new StringBuilder();

        sql.append(" select id, code, full_name, city_id, district_id from wards where status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        if (!ids.isEmpty()) {
            sql.append(" and id not in (:ids) ");
            sqlParameterSource.addValue("ids", ids);
        }

        if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and id = :id ");
            sqlParameterSource.addValue("id", request.getWardId());
        }

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<Ward> wards = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(Ward.class));

        List<WardDto> wardDtos = new ArrayList<>();

        wards.forEach(ward -> {
            WardDto wardDto = modelMapper.map(ward, WardDto.class);
            wardDto.setCity(findCityByIdWithoutAuditor(wardDto.getCityId()));
            wardDto.setDistrict(findDistrictByIdWithoutAuditor(wardDto.getDistrictId()));
            wardDtos.add(wardDto);
        });

        return new SuccessResponse<>(wardDtos);
    }
}
