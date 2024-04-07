package com.system.management.service;

import com.system.management.model.dto.TreatmentPlaceDto;
import com.system.management.model.entity.TreatmentPlace;
import com.system.management.model.request.treatment_place.GetListTreatmentPlaceRequest;
import com.system.management.model.request.treatment_place.InsertTreatmentPlacePlaceRequest;
import com.system.management.model.request.treatment_place.UpdateTreatmentPlacePlaceRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.TreatmentPlaceRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.StatusEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentPlaceService extends BaseCommonService {

    private final TreatmentPlaceRepository treatmentPlaceRepository;

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> insert(InsertTreatmentPlacePlaceRequest request) {
        Long cityId = request.getCityId();
        if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        Long districtId = request.getDistrictId();
        if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        Long wardId = request.getWardId();
        if (!FunctionUtils.isNullOrZero(request.getWardId()) && (!wardRepository.existsByIdAndStatus(wardId, ACTIVE.name()))) {
            throw new BadRequestException(WARD_NOT_EXISTS);
        }

        TreatmentPlace treatmentPlace = new TreatmentPlace();
        treatmentPlace.setFullName(request.getFullName());
        treatmentPlace.setAddressDetail(request.getAddressDetail());
        treatmentPlace.setLeaderFullName(request.getLeaderFullName());
        treatmentPlace.setLeaderIdentifyNumber(request.getLeaderIdentifyNumber());
        treatmentPlace.setLeaderPhoneNumber(request.getLeaderPhoneNumber());
        treatmentPlace.setLeaderEmail(request.getLeaderEmail());
        treatmentPlace.setStatus(StatusEnums.ACTIVE.name());
        treatmentPlace.setCityId(cityId);
        treatmentPlace.setDistrictId(districtId);
        treatmentPlace.setWardId(wardId);

        TreatmentPlaceDto treatmentPlaceDto = modelMapper.map(treatmentPlaceRepository.save(treatmentPlace), TreatmentPlaceDto.class);
        setCadastralInfo(treatmentPlaceDto);

        return new SuccessResponse<>(treatmentPlaceDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdateTreatmentPlacePlaceRequest request) {
        Long cityId = request.getCityId();
        if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        Long districtId = request.getDistrictId();
        if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        Long wardId = request.getWardId();
        if (!FunctionUtils.isNullOrZero(request.getWardId()) && (!wardRepository.existsByIdAndStatus(wardId, ACTIVE.name()))) {
            throw new BadRequestException(WARD_NOT_EXISTS);
        }

        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(request.getId(), StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        treatmentPlace.setFullName(request.getFullName());
        treatmentPlace.setAddressDetail(request.getAddressDetail());
        treatmentPlace.setLeaderFullName(request.getLeaderFullName());
        treatmentPlace.setLeaderIdentifyNumber(request.getLeaderIdentifyNumber());
        treatmentPlace.setLeaderPhoneNumber(request.getLeaderPhoneNumber());
        treatmentPlace.setLeaderEmail(request.getLeaderEmail());
        treatmentPlace.setCityId(cityId);
        treatmentPlace.setDistrictId(districtId);
        treatmentPlace.setWardId(wardId);

        TreatmentPlaceDto treatmentPlaceDto = modelMapper.map(treatmentPlaceRepository.save(treatmentPlace), TreatmentPlaceDto.class);
        setCadastralInfo(treatmentPlaceDto);

        return new SuccessResponse<>(treatmentPlaceDto);
    }

    public SuccessResponse<Object> delete(Long id) {
        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(id, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        treatmentPlace.setStatus(DELETED.name());
        treatmentPlaceRepository.save(treatmentPlace);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListTreatmentPlaceRequest request) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select * from detox_places where 1 = 1 ");

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and full_name like concat(:full_name, '%') ");
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        if (StringUtils.isNotBlank(request.getLeaderFullName())) {
            sql.append(" and leader_full_name like concat(:leader_full_name, '%') ");
            sqlParameterSource.addValue("leader_full_name", request.getLeaderFullName());
        }

        if (StringUtils.isNotBlank(request.getLeaderPhoneNumber())) {
            sql.append(" and leader_phone_number like concat(:leader_phone_number, '%') ");
            sqlParameterSource.addValue("leader_phone_number", request.getLeaderPhoneNumber());
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

        List<TreatmentPlaceDto> treatmentPlaces = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(TreatmentPlaceDto.class));

        treatmentPlaces.forEach(this::setCadastralInfo);

        return new SuccessResponse<>(treatmentPlaces);
    }

    public SuccessResponse<Object> get(Long id) {
        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(id, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));
        TreatmentPlaceDto treatmentPlaceDto = modelMapper.map(treatmentPlace, TreatmentPlaceDto.class);
        setCadastralInfo(treatmentPlaceDto);
        return new SuccessResponse<>(treatmentPlaceDto);
    }
}
