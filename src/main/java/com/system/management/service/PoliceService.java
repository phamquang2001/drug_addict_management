package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.Police;
import com.system.management.model.request.police.GetListPoliceRequest;
import com.system.management.model.request.police.InsertPoliceRequest;
import com.system.management.model.request.police.UpdatePoliceRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.GenderEnums;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.enums.StatusEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoliceService extends BaseCommonService {

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> insert(InsertPoliceRequest request) {

        Integer levelValue = request.getLevel();
        Long cityId = request.getCityId();
        Long districtId = request.getDistrictId();
        Long wardId = request.getWardId();

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

        if (policeRepository.existsByIdentifyNumberAndStatus(request.getIdentifyNumber(), StatusEnums.ACTIVE.name())) {
            throw new BadRequestException(INVALID_IDENTIFY_NUMBER);
        }

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        LevelEnums level = LevelEnums.dict.get(levelValue);
        if (level == null) {
            throw new BadRequestException(INVALID_LEVEL);
        }

        String password = FunctionUtils.generatePassword();

        Police police = new Police();
        police.setIdentifyNumber(request.getIdentifyNumber());
        police.setPassword(passwordEncoder.encode(password));
        police.setFullName(request.getFullName());
        police.setGender(gender.value);
        police.setDateOfBirth(request.getDateOfBirth());
        police.setPhoneNumber(request.getPhoneNumber());
        police.setEmail(request.getEmail());
        police.setLevel(level.value);
        police.setRole(RoleEnums.POLICE.value);
        police.setStatus(StatusEnums.ACTIVE.name());
        police.setCityId(cityId);
        police.setDistrictId(districtId);
        police.setWardId(wardId);

        police = policeRepository.save(police);
        PoliceDto policeDto = convertToPoliceDto(police);

        emailService.sendMailAccountCreated(policeDto, password);

        return new SuccessResponse<>(policeDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdatePoliceRequest request) {

        Integer levelValue = request.getLevel();
        Long cityId = request.getCityId();
        Long districtId = request.getDistrictId();
        Long wardId = request.getWardId();

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

        Police police = policeRepository
                .findByIdAndStatus(request.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        RoleEnums role = RoleEnums.dict.get(request.getRole());
        if (role == null) {
            throw new BadRequestException(INVALID_ROLE);
        }

        LevelEnums level = LevelEnums.dict.get(request.getLevel());
        if (level == null) {
            throw new BadRequestException(INVALID_LEVEL);
        }

        police.setFullName(request.getFullName());
        police.setGender(gender.value);
        police.setDateOfBirth(request.getDateOfBirth());
        police.setPhoneNumber(request.getPhoneNumber());
        police.setEmail(request.getEmail());
        police.setLevel(level.value);
        police.setRole(role.value);
        police.setCityId(cityId);
        police.setDistrictId(districtId);
        police.setWardId(wardId);

        police = policeRepository.save(police);

        return new SuccessResponse<>(convertToPoliceDto(police));
    }

    public SuccessResponse<Object> delete(Long id) {
        Police police = policeRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        police.setStatus(DELETED.name());
        policeRepository.save(police);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListPoliceRequest request) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select * from polices where 1 = 1 ");

        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {
            sql.append(" and identify_number like concat(:identify_number, '%') ");
            sqlParameterSource.addValue("identify_number", request.getIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and full_name like concat(:full_name, '%') ");
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        if (!FunctionUtils.isNullOrZero(request.getRole())) {
            sql.append(" and role = :role ");
            sqlParameterSource.addValue("role", request.getRole());
        }

        if (!FunctionUtils.isNullOrZero(request.getLevel())) {
            sql.append(" and level = :level ");
            sqlParameterSource.addValue("level", request.getLevel());
        }

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

        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 10 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<PoliceDto> polices = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(PoliceDto.class));

        polices.forEach(police -> {
            police.setRoleName(RoleEnums.dict.get(police.getRole()).label);
            police.setLevelName(LevelEnums.dict.get(police.getLevel()).label);
            setCadastralInfo(police);
        });

        return new SuccessResponse<>(polices);
    }

    public SuccessResponse<Object> get(Long id) {
        Police police = policeRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));
        return new SuccessResponse<>(convertToPoliceDto(police));
    }
}
