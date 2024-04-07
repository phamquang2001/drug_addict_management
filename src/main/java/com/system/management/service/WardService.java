package com.system.management.service;

import com.system.management.model.dto.WardDto;
import com.system.management.model.entity.Ward;
import com.system.management.model.request.ward.GetListWardRequest;
import com.system.management.model.request.ward.InsertWardRequest;
import com.system.management.model.request.ward.UpdateWardRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.exception.BadRequestException;
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
public class WardService extends BaseCommonService {

    public SuccessResponse<Object> insert(InsertWardRequest request) {

        Long cityId = request.getCityId();
        if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        Long districtId = request.getDistrictId();
        if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        if (wardRepository.existsByCodeAndStatusAndDistrictIdAndCityId(request.getCode(), ACTIVE.name(), districtId, cityId)) {
            throw new ProcessException("Trong quận huyện đã tồn tại phường xã có mã truyền xuống");
        }

        Ward ward = new Ward();
        ward.setCityId(cityId);
        ward.setDistrictId(districtId);
        ward.setStatus(ACTIVE.name());
        ward.setCode(request.getCode());
        ward.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));
        ward.setUnsignedName(FunctionUtils.normalizeAndLowercase(ward.getFullName()));
        return new SuccessResponse<>(modelMapper.map(wardRepository.save(ward), WardDto.class));
    }

    public SuccessResponse<Object> update(UpdateWardRequest request) {

        Long cityId = request.getCityId();
        if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        Long districtId = request.getDistrictId();
        if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        Long wardId = request.getId();
        Ward ward = wardRepository
                .findByIdAndStatus(wardId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(WARD_NOT_EXISTS));

        if (wardRepository.existsByCodeAndStatusAndIdNotAndDistrictIdAndCityId(request.getCode(), ACTIVE.name(), wardId, districtId, cityId)) {
            throw new ProcessException("Trong quận huyện đã tồn tại phường xã có mã truyền xuống");
        }

        ward.setCityId(cityId);
        ward.setDistrictId(districtId);
        ward.setCode(request.getCode());
        ward.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));
        ward.setUnsignedName(FunctionUtils.normalizeAndLowercase(ward.getFullName()));
        return new SuccessResponse<>(modelMapper.map(wardRepository.save(ward), WardDto.class));
    }

    public SuccessResponse<Object> delete(Long id) {
        Ward ward = wardRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(WARD_NOT_EXISTS));
        ward.setStatus(DELETED.name());
        wardRepository.save(ward);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListWardRequest request) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select id, code, full_name, city_id, district_id status from wards where 1 = 1 ");

        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (StringUtils.isNotBlank(request.getCode())) {
            sql.append(" and code = :code ");
            sqlParameterSource.addValue("code", request.getCode());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and unsigned_name like concat(:name, '%') ");
            sqlParameterSource.addValue("name", FunctionUtils.normalizeAndLowercase(request.getFullName()));
        }

        String status = request.getStatus();
        if (StringUtils.isBlank(status)) status = ACTIVE.name();

        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", status.toUpperCase());

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 10 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<WardDto> wards = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(WardDto.class));

        wards.forEach(item -> {
            item.setCity(findCityByIdWithoutAuditor(item.getCityId()));
            item.setDistrict(findDistrictByIdWithoutAuditor(item.getDistrictId()));
        });

        return new SuccessResponse<>(wards);
    }
}
