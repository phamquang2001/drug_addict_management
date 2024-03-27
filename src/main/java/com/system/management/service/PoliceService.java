package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.City;
import com.system.management.model.entity.District;
import com.system.management.model.entity.Police;
import com.system.management.model.entity.Ward;
import com.system.management.model.request.police.GetListPoliceRequest;
import com.system.management.model.request.police.InsertPoliceRequest;
import com.system.management.model.request.police.UpdatePoliceRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.CityRepository;
import com.system.management.repository.DistrictRepository;
import com.system.management.repository.PoliceRepository;
import com.system.management.repository.WardRepository;
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
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.system.management.utils.constants.ErrorMessage.POLICE_NOT_EXISTS;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoliceService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final MapSqlParameterSource sqlParameterSource;

    private final CityRepository cityRepository;

    private final DistrictRepository districtRepository;

    private final WardRepository wardRepository;

    private final PoliceRepository policeRepository;

    private final PasswordEncoder passwordEncoder;

    private final FunctionUtils functionUtils;

    private final EmailService emailService;

    private final ModelMapper modelMapper;

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> insert(InsertPoliceRequest request) {
        if (policeRepository.existsByIdentifyNumberAndStatus(request.getIdentifyNumber(), StatusEnums.ACTIVE.name())) {
            throw new BadRequestException("Số cccd đã được tạo tài khoản!");
        }

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException("Giới tính không hợp lệ! Vui lòng nhập 1 (Nam) hoặc 2 (Nữ)");
        }

        LevelEnums level = LevelEnums.dict.get(request.getLevel());
        if (level == null) {
            throw new BadRequestException("Cấp bậc tài khoản không hợp lệ!");
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

        if (request.getLevel() > 1) {
            City city = (City) FunctionUtils.validateIdAndExistence(request.getCityId(), cityRepository, "Tỉnh/Thành phố");
            police.setCity(city);
        }

        if (request.getLevel() > 2) {
            District district = (District) FunctionUtils.validateIdAndExistence(request.getDistrictId(), districtRepository, "Quận/Huyện");
            police.setDistrict(district);
        }

        if (request.getLevel() > 3) {
            Ward ward = (Ward) FunctionUtils.validateIdAndExistence(request.getWardId(), wardRepository, "Phường/Xã");
            police.setWard(ward);
        }

        PoliceDto policeDto = modelMapper.map(policeRepository.save(police), PoliceDto.class);
        policeDto.setRoleName(RoleEnums.dict.get(police.getRole()).label);
        policeDto.setLevelName(LevelEnums.dict.get(police.getLevel()).label);
        functionUtils.setCadastralInfo(policeDto);

//        emailService.sendMailAccountCreated(policeDto, password);

        return new SuccessResponse<>(policeDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdatePoliceRequest request) {
        Police police = policeRepository
                .findByIdAndStatus(request.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException("Giới tính không hợp lệ! Vui lòng nhập 1 (Nam) hoặc 2 (Nữ)");
        }

        RoleEnums role = RoleEnums.dict.get(request.getRole());
        if (role == null) {
            throw new BadRequestException("Vai trò tài khoản không hợp lệ!");
        }

        LevelEnums level = LevelEnums.dict.get(request.getLevel());
        if (level == null) {
            throw new BadRequestException("Cấp bậc tài khoản không hợp lệ!");
        }

        police.setFullName(request.getFullName());
        police.setGender(gender.value);
        police.setDateOfBirth(request.getDateOfBirth());
        police.setPhoneNumber(request.getPhoneNumber());
        police.setEmail(request.getEmail());
        police.setLevel(level.value);
        police.setRole(role.value);

        if (request.getLevel() > 1) {
            City city = (City) FunctionUtils.validateIdAndExistence(request.getCityId(), cityRepository, "Tỉnh/Thành phố");
            police.setCity(city);
        }

        if (request.getLevel() > 2) {
            District district = (District) FunctionUtils.validateIdAndExistence(request.getDistrictId(), districtRepository, "Quận/Huyện");
            police.setDistrict(district);
        }

        if (request.getLevel() > 3) {
            Ward ward = (Ward) FunctionUtils.validateIdAndExistence(request.getWardId(), wardRepository, "Phường/Xã");
            police.setWard(ward);
        }

        PoliceDto policeDto = modelMapper.map(policeRepository.save(police), PoliceDto.class);
        policeDto.setRoleName(RoleEnums.dict.get(police.getRole()).label);
        policeDto.setLevelName(LevelEnums.dict.get(police.getLevel()).label);
        functionUtils.setCadastralInfo(policeDto);
        return new SuccessResponse<>(policeDto);
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
            functionUtils.setCadastralInfo(police);
        });

        return new SuccessResponse<>(polices);
    }

    public SuccessResponse<Object> get(Long id) {
        Police police = policeRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));
        PoliceDto policeDto = modelMapper.map(police, PoliceDto.class);
        policeDto.setRoleName(RoleEnums.dict.get(police.getRole()).label);
        policeDto.setLevelName(LevelEnums.dict.get(police.getLevel()).label);
        functionUtils.setCadastralInfo(policeDto);
        return new SuccessResponse<>(policeDto);
    }
}
