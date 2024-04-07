package com.system.management.service;

import com.system.management.model.dto.DistrictDto;
import com.system.management.model.entity.District;
import com.system.management.model.request.district.GetListDistrictRequest;
import com.system.management.model.request.district.InsertDistrictRequest;
import com.system.management.model.request.district.UpdateDistrictRequest;
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

import static com.system.management.utils.constants.ErrorMessage.CITY_NOT_EXISTS;
import static com.system.management.utils.constants.ErrorMessage.DISTRICT_NOT_EXISTS;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictService extends BaseCommonService {

    public SuccessResponse<Object> insert(InsertDistrictRequest request) {
        Long cityId = request.getCityId();

        if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        if (districtRepository.existsByCodeAndStatusAndCityId(request.getCode(), ACTIVE.name(), cityId)) {
            throw new ProcessException("Trong tỉnh thành phố đã tồn tại quận huyện có mã truyền xuống");
        }

        District district = new District();
        district.setCityId(cityId);
        district.setStatus(ACTIVE.name());
        district.setCode(request.getCode());
        district.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));
        district.setUnsignedName(FunctionUtils.normalizeAndLowercase(district.getFullName()));
        return new SuccessResponse<>(modelMapper.map(districtRepository.save(district), DistrictDto.class));
    }

    public SuccessResponse<Object> update(UpdateDistrictRequest request) {
        Long cityId = request.getCityId();

        if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        Long districtId = request.getId();
        District district = districtRepository
                .findByIdAndStatus(districtId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DISTRICT_NOT_EXISTS));

        if (districtRepository.existsByCodeAndStatusAndIdNotAndCityId(request.getCode(), ACTIVE.name(), districtId, cityId)) {
            throw new ProcessException("Trong tỉnh thành phố đã tồn tại quận huyện có mã truyền xuống");
        }

        district.setCityId(cityId);
        district.setCode(request.getCode());
        district.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));
        district.setUnsignedName(FunctionUtils.normalizeAndLowercase(district.getFullName()));
        return new SuccessResponse<>(modelMapper.map(districtRepository.save(district), DistrictDto.class));
    }

    public SuccessResponse<Object> delete(Long id) {
        District district = districtRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DISTRICT_NOT_EXISTS));
        district.setStatus(DELETED.name());
        districtRepository.save(district);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListDistrictRequest request) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select id, code, full_name, city_id, status from districts where 1 = 1 ");

        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
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

        List<DistrictDto> districts = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(DistrictDto.class));

        districts.forEach(item -> item.setCity(findCityByIdWithoutAuditor(item.getCityId())));

        return new SuccessResponse<>(districts);
    }
}
