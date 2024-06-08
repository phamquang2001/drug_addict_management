package com.system.management.service;

import com.system.management.model.dto.CityDto;
import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.City;
import com.system.management.model.request.city.GetListCityRequest;
import com.system.management.model.request.city.InsertCityRequest;
import com.system.management.model.request.city.UpdateCityRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.exception.ForbiddenException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService extends BaseCommonService {

    public SuccessResponse<Object> insert(InsertCityRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (cityRepository.existsByCodeAndStatus(request.getCode(), ACTIVE.name())) {
            throw new ProcessException(CITY_EXISTS_WITH_CODE);
        }

        City city = new City();
        city.setStatus(ACTIVE.name());
        city.setCode(request.getCode());
        city.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));
        city.setUnsignedName(FunctionUtils.normalizeAndLowercase(request.getFullName()));
        return new SuccessResponse<>(modelMapper.map(cityRepository.save(city), CityDto.class));
    }

    public SuccessResponse<Object> update(UpdateCityRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        City city = cityRepository
                .findByIdAndStatus(request.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(CITY_NOT_EXISTS));

        if (cityRepository.existsByCodeAndStatusAndIdNot(request.getCode(), ACTIVE.name(), city.getId())) {
            throw new ProcessException(CITY_EXISTS_WITH_CODE);
        }

        city.setCode(request.getCode());
        city.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));
        city.setUnsignedName(FunctionUtils.normalizeAndLowercase(request.getFullName()));
        return new SuccessResponse<>(modelMapper.map(cityRepository.save(city), CityDto.class));
    }

    public SuccessResponse<Object> delete(Long id) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        City city = cityRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(CITY_NOT_EXISTS));
        city.setStatus(DELETED.name());
        cityRepository.save(city);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListCityRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        StringBuilder sql = new StringBuilder();

        sql.append(" select id, code, full_name, status from cities where 1 = 1 ");

        if (StringUtils.isNotBlank(request.getCode())) {
            sql.append(" and code = :code ");
            sqlParameterSource.addValue("code", request.getCode());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and unsigned_name like concat('%', :name, '%') ");
            sqlParameterSource.addValue("name", FunctionUtils.normalizeAndLowercase(request.getFullName()));
        }

        String status = request.getStatus();
        if (StringUtils.isBlank(status)) status = ACTIVE.name();

        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", status.toUpperCase());

        sql.append(" order by code ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<CityDto> cities = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(CityDto.class));

        return new SuccessResponse<>(cities);
    }
}
