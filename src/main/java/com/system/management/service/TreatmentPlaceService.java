package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.dto.TreatmentPlaceDto;
import com.system.management.model.entity.TreatmentPlace;
import com.system.management.model.request.treatment_place.GetListTreatmentPlaceRequest;
import com.system.management.model.request.treatment_place.InsertTreatmentPlacePlaceRequest;
import com.system.management.model.request.treatment_place.UpdateTreatmentPlacePlaceRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.TreatmentPlaceRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.enums.StatusEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ForbiddenException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
        } else {
            cityId = request.getCityId();
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
        } else {
            districtId = request.getDistrictId();
            if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        Long wardId = null;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            wardId = loggedAccount.getWardId();
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            wardId = request.getWardId();
            if (!wardRepository.existsByIdAndStatus(wardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
        }

        TreatmentPlace treatmentPlace = new TreatmentPlace();
        treatmentPlace.setFullName(request.getFullName());
        treatmentPlace.setAddressDetail(request.getAddressDetail());
        treatmentPlace.setLeaderFullName(request.getLeaderFullName());
        treatmentPlace.setLeaderIdentifyNumber(request.getLeaderIdentifyNumber());
        treatmentPlace.setLeaderPhoneNumber(request.getLeaderPhoneNumber());
        treatmentPlace.setLeaderEmail(request.getLeaderEmail());
        treatmentPlace.setStatus(ACTIVE.name());
        treatmentPlace.setCityId(cityId);
        treatmentPlace.setDistrictId(districtId);
        treatmentPlace.setWardId(wardId);
        treatmentPlace = treatmentPlaceRepository.save(treatmentPlace);

        return new SuccessResponse<>(convertToTreatmentPlaceDto(treatmentPlace));
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdateTreatmentPlacePlaceRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(request.getId(), StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
            if (!cityId.equals(treatmentPlace.getCityId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {
            cityId = request.getCityId();
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
            if (!districtId.equals(treatmentPlace.getDistrictId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {
            districtId = request.getDistrictId();
            if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        Long wardId = null;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            wardId = loggedAccount.getWardId();
            if (!wardId.equals(treatmentPlace.getWardId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            wardId = request.getWardId();
            if (!wardRepository.existsByIdAndStatus(wardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
        }

        treatmentPlace.setFullName(request.getFullName());
        treatmentPlace.setAddressDetail(request.getAddressDetail());
        treatmentPlace.setLeaderFullName(request.getLeaderFullName());
        treatmentPlace.setLeaderIdentifyNumber(request.getLeaderIdentifyNumber());
        treatmentPlace.setLeaderPhoneNumber(request.getLeaderPhoneNumber());
        treatmentPlace.setLeaderEmail(request.getLeaderEmail());
        treatmentPlace.setCityId(cityId);
        treatmentPlace.setDistrictId(districtId);
        treatmentPlace.setWardId(wardId);
        treatmentPlace = treatmentPlaceRepository.save(treatmentPlace);

        return new SuccessResponse<>(convertToTreatmentPlaceDto(treatmentPlace));
    }

    public SuccessResponse<Object> delete(Long id) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(id, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(treatmentPlace.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(treatmentPlace.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(treatmentPlace.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        treatmentPlace.setStatus(DELETED.name());
        treatmentPlaceRepository.save(treatmentPlace);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListTreatmentPlaceRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();

        StringBuilder sql = new StringBuilder();

        sql.append(" select *, created_by as txt_created_by, modified_by as txt_modified_by from treatment_places where 1 = 1 ");

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and full_name like concat('%', :full_name, '%') ");
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        if (StringUtils.isNotBlank(request.getLeaderFullName())) {
            sql.append(" and leader_full_name like concat('%', :leader_full_name, '%') ");
            sqlParameterSource.addValue("leader_full_name", request.getLeaderFullName());
        }

        if (StringUtils.isNotBlank(request.getLeaderPhoneNumber())) {
            sql.append(" and leader_phone_number like concat('%', :leader_phone_number, '%') ");
            sqlParameterSource.addValue("leader_phone_number", request.getLeaderPhoneNumber());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());
        } else if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            sql.append(" and district_id = :district_id ");
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());
        } else if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            sql.append(" and ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", loggedAccount.getWardId());
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) {
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

        List<TreatmentPlace> treatmentPlaces = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(TreatmentPlace.class));

        List<TreatmentPlaceDto> treatmentPlaceDtos = new ArrayList<>();

        treatmentPlaces.forEach(treatmentPlace -> treatmentPlaceDtos.add(convertToTreatmentPlaceDto(treatmentPlace)));

        return new SuccessResponse<>(treatmentPlaceDtos);
    }

    public SuccessResponse<Object> get(Long id) {

        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(id, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        PoliceDto loggedAccount = getLoggedAccount();

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(treatmentPlace.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(treatmentPlace.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(treatmentPlace.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        return new SuccessResponse<>(convertToTreatmentPlaceDto(treatmentPlace));
    }
}
