package com.system.management.service;

import com.system.management.model.dto.DrugAddictDto;
import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.DrugAddict;
import com.system.management.model.entity.DrugAddictRequest;
import com.system.management.model.request.drug_addict.GetListDrugAddictRequest;
import com.system.management.model.request.drug_addict.InsertDrugAddictRequest;
import com.system.management.model.request.drug_addict.UpdateDrugAddictRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.DrugAddictRepository;
import com.system.management.repository.DrugAddictRequestRepository;
import com.system.management.repository.TreatmentPlaceRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.GenderEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.enums.StatusEnums;
import com.system.management.utils.exception.BadRequestException;
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
public class DrugAddictService extends BaseCommonService {

    private final DrugAddictRepository drugAddictRepository;

    private final TreatmentPlaceRepository treatmentPlaceRepository;

    private final DrugAddictRequestRepository drugAddictRequestRepository;

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> insert(InsertDrugAddictRequest request) {

        Long permanentWardId = request.getPermanentWardId();
        Long permanentDistrictId = request.getPermanentDistrictId();
        Long permanentCityId = request.getPermanentCityId();
        String permanentAddressDetail = request.getPermanentAddressDetail();

        if (!cityRepository.existsByIdAndStatus(permanentCityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        if (!districtRepository.existsByIdAndStatus(permanentDistrictId, ACTIVE.name())) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        if (!wardRepository.existsByIdAndStatus(permanentWardId, ACTIVE.name())) {
            throw new BadRequestException(WARD_NOT_EXISTS);
        }

        if (drugAddictRepository.existsByIdentifyNumberAndStatus(request.getIdentifyNumber(), StatusEnums.ACTIVE.name())) {
            throw new BadRequestException(INVALID_IDENTIFY_NUMBER);
        }

        Long treatmentPlaceId = request.getTreatmentPlaceId();
        if (!FunctionUtils.isNullOrZero(treatmentPlaceId)
                && !treatmentPlaceRepository.existsByIdAndStatus(treatmentPlaceId, ACTIVE.name())) {
            throw new BadRequestException(TREATMENT_PLACE_NOT_EXISTS);
        }

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        DrugAddict drugAddict = new DrugAddict();
        drugAddict.setIdentifyNumber(request.getIdentifyNumber());
        drugAddict.setFullName(request.getFullName());
        drugAddict.setGender(gender.value);
        drugAddict.setDateOfBirth(request.getDateOfBirth());
        drugAddict.setPhoneNumber(request.getPhoneNumber());
        drugAddict.setEmail(request.getEmail());
        drugAddict.setTreatmentPlaceId(treatmentPlaceId);
        drugAddict.setPermanentWardId(permanentWardId);
        drugAddict.setPermanentDistrictId(permanentDistrictId);
        drugAddict.setPermanentCityId(permanentCityId);
        drugAddict.setPermanentAddressDetail(permanentAddressDetail);
        drugAddict.setIsAtPermanent(request.getIsAtPermanent());

        if (Boolean.TRUE.equals(request.getIsAtPermanent())) {
            drugAddict.setCurrentWardId(permanentWardId);
            drugAddict.setCurrentDistrictId(permanentDistrictId);
            drugAddict.setCurrentCityId(permanentCityId);
            drugAddict.setCurrentAddressDetail(permanentAddressDetail);
        } else {
            Long currentWardId = request.getCurrentWardId();
            Long currentDistrictId = request.getCurrentDistrictId();
            Long currentCityId = request.getCurrentCityId();
            String currentAddressDetail = request.getCurrentAddressDetail();

            if (FunctionUtils.isNullOrZero(currentCityId)
                    || !cityRepository.existsByIdAndStatus(currentCityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }

            if (FunctionUtils.isNullOrZero(currentDistrictId)
                    || !districtRepository.existsByIdAndStatus(currentDistrictId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }

            if (FunctionUtils.isNullOrZero(currentWardId)
                    || !wardRepository.existsByIdAndStatus(currentWardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }

            drugAddict.setCurrentWardId(currentWardId);
            drugAddict.setCurrentDistrictId(currentDistrictId);
            drugAddict.setCurrentCityId(currentCityId);
            drugAddict.setCurrentAddressDetail(currentAddressDetail);
        }

        drugAddict = drugAddictRepository.save(drugAddict);

        return new SuccessResponse<>(convertToDrugAddictDto(drugAddict));
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdateDrugAddictRequest request) {

        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(request.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

        Long permanentWardId = request.getPermanentWardId();
        Long permanentDistrictId = request.getPermanentDistrictId();
        Long permanentCityId = request.getPermanentCityId();
        String permanentAddressDetail = request.getPermanentAddressDetail();

        if (!cityRepository.existsByIdAndStatus(permanentCityId, ACTIVE.name())) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        if (!districtRepository.existsByIdAndStatus(permanentDistrictId, ACTIVE.name())) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        if (!wardRepository.existsByIdAndStatus(permanentWardId, ACTIVE.name())) {
            throw new BadRequestException(WARD_NOT_EXISTS);
        }

        Long treatmentPlaceId = request.getTreatmentPlaceId();
        if (!FunctionUtils.isNullOrZero(treatmentPlaceId)
                && !treatmentPlaceRepository.existsByIdAndStatus(treatmentPlaceId, ACTIVE.name())) {
            throw new BadRequestException(TREATMENT_PLACE_NOT_EXISTS);
        }

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        PoliceDto loggedAccount = getLoggedAccount();

        SuccessResponse<Object> response;

        if (Objects.equals(loggedAccount.getRole(), RoleEnums.POLICE.value)) {

            DrugAddictRequest drugAddictRequest = new DrugAddictRequest();
            drugAddictRequest.setDrugAddictId(drugAddict.getId());
            drugAddictRequest.setIdentifyNumber(drugAddict.getIdentifyNumber());
            drugAddictRequest.setFullName(request.getFullName());
            drugAddictRequest.setGender(gender.value);
            drugAddictRequest.setDateOfBirth(request.getDateOfBirth());
            drugAddictRequest.setPhoneNumber(request.getPhoneNumber());
            drugAddictRequest.setEmail(request.getEmail());
            drugAddictRequest.setTreatmentPlaceId(treatmentPlaceId);
            drugAddictRequest.setPermanentWardId(permanentWardId);
            drugAddictRequest.setPermanentDistrictId(permanentDistrictId);
            drugAddictRequest.setPermanentCityId(permanentCityId);
            drugAddictRequest.setPermanentAddressDetail(permanentAddressDetail);
            drugAddictRequest.setIsAtPermanent(request.getIsAtPermanent());

            if (Boolean.TRUE.equals(request.getIsAtPermanent())) {
                drugAddictRequest.setCurrentWardId(permanentWardId);
                drugAddictRequest.setCurrentDistrictId(permanentDistrictId);
                drugAddictRequest.setCurrentCityId(permanentCityId);
                drugAddictRequest.setCurrentAddressDetail(permanentAddressDetail);
            } else {
                Long currentWardId = request.getCurrentWardId();
                Long currentDistrictId = request.getCurrentDistrictId();
                Long currentCityId = request.getCurrentCityId();
                String currentAddressDetail = request.getCurrentAddressDetail();

                if (FunctionUtils.isNullOrZero(currentCityId)
                        || !cityRepository.existsByIdAndStatus(currentCityId, ACTIVE.name())) {
                    throw new BadRequestException(CITY_NOT_EXISTS);
                }

                if (FunctionUtils.isNullOrZero(currentDistrictId)
                        || !districtRepository.existsByIdAndStatus(currentDistrictId, ACTIVE.name())) {
                    throw new BadRequestException(DISTRICT_NOT_EXISTS);
                }

                if (FunctionUtils.isNullOrZero(currentWardId)
                        || !wardRepository.existsByIdAndStatus(currentWardId, ACTIVE.name())) {
                    throw new BadRequestException(WARD_NOT_EXISTS);
                }

                drugAddictRequest.setCurrentWardId(currentWardId);
                drugAddictRequest.setCurrentDistrictId(currentDistrictId);
                drugAddictRequest.setCurrentCityId(currentCityId);
                drugAddictRequest.setCurrentAddressDetail(currentAddressDetail);
            }

            drugAddictRequest = drugAddictRequestRepository.save(drugAddictRequest);

            response = new SuccessResponse<>(convertToDrugAddictRequestDto(drugAddictRequest));

        } else {

            drugAddict.setFullName(request.getFullName());
            drugAddict.setGender(gender.value);
            drugAddict.setDateOfBirth(request.getDateOfBirth());
            drugAddict.setPhoneNumber(request.getPhoneNumber());
            drugAddict.setEmail(request.getEmail());
            drugAddict.setTreatmentPlaceId(treatmentPlaceId);
            drugAddict.setPermanentWardId(permanentWardId);
            drugAddict.setPermanentDistrictId(permanentDistrictId);
            drugAddict.setPermanentCityId(permanentCityId);
            drugAddict.setPermanentAddressDetail(permanentAddressDetail);
            drugAddict.setIsAtPermanent(request.getIsAtPermanent());

            if (Boolean.TRUE.equals(request.getIsAtPermanent())) {
                drugAddict.setCurrentWardId(permanentWardId);
                drugAddict.setCurrentDistrictId(permanentDistrictId);
                drugAddict.setCurrentCityId(permanentCityId);
                drugAddict.setCurrentAddressDetail(permanentAddressDetail);
            } else {
                Long currentWardId = request.getCurrentWardId();
                Long currentDistrictId = request.getCurrentDistrictId();
                Long currentCityId = request.getCurrentCityId();
                String currentAddressDetail = request.getCurrentAddressDetail();

                if (FunctionUtils.isNullOrZero(currentCityId)
                        || !cityRepository.existsByIdAndStatus(currentCityId, ACTIVE.name())) {
                    throw new BadRequestException(CITY_NOT_EXISTS);
                }

                if (FunctionUtils.isNullOrZero(currentDistrictId)
                        || !districtRepository.existsByIdAndStatus(currentDistrictId, ACTIVE.name())) {
                    throw new BadRequestException(DISTRICT_NOT_EXISTS);
                }

                if (FunctionUtils.isNullOrZero(currentWardId)
                        || !wardRepository.existsByIdAndStatus(currentWardId, ACTIVE.name())) {
                    throw new BadRequestException(WARD_NOT_EXISTS);
                }

                drugAddict.setCurrentWardId(currentWardId);
                drugAddict.setCurrentDistrictId(currentDistrictId);
                drugAddict.setCurrentCityId(currentCityId);
                drugAddict.setCurrentAddressDetail(currentAddressDetail);
            }

            drugAddict = drugAddictRepository.save(drugAddict);

            response = new SuccessResponse<>(convertToDrugAddictDto(drugAddict));
        }

        return response;
    }

    public SuccessResponse<Object> delete(Long id) {
        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));
        drugAddict.setStatus(DELETED.name());
        drugAddictRepository.save(drugAddict);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListDrugAddictRequest request) {
        StringBuilder sql = new StringBuilder();

        sql.append(" select * from drug_addicts da left join polices p on da.police_id = p.id where 1 = 1 ");

        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {
            sql.append(" and da.identify_number like concat(:da_identify_number, '%') ");
            sqlParameterSource.addValue("da_identify_number", request.getIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and da.full_name like concat(:da_full_name, '%') ");
            sqlParameterSource.addValue("da_full_name", request.getFullName());
        }

        if (!FunctionUtils.isNullOrZero(request.getSupervisorStatus())) {
            sql.append(" and da.police_id is not null ");
        }

        if (!FunctionUtils.isNullOrZero(request.getSupervisorLevel())) {
            sql.append(" and p.level = :level ");
            sqlParameterSource.addValue("level", request.getSupervisorLevel());
        }

        if (StringUtils.isNotBlank(request.getSupervisorIdentifyNumber())) {
            sql.append(" and da.identify_number like concat(:p_identify_number, '%') ");
            sqlParameterSource.addValue("p_identify_number", request.getSupervisorIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getSupervisorFullName())) {
            sql.append(" and p.full_name like concat(:p_full_name, '%') ");
            sqlParameterSource.addValue("p_full_name", request.getSupervisorFullName());
        }

        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and da.permanent_city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and da.permanent_district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and da.permanent_ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        if (!FunctionUtils.isNullOrZero(request.getTreatmentPlaceId())) {
            sql.append(" and da.treatment_places_id = :treatment_places_id ");
            sqlParameterSource.addValue("treatment_places_id", request.getTreatmentPlaceId());
        }

        sql.append(" and da.status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        sql.append(" order by da.created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 10 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<DrugAddict> drugAddicts = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(DrugAddict.class));

        List<DrugAddictDto> drugAddictDtos = new ArrayList<>();

        drugAddicts.forEach(item -> drugAddictDtos.add(convertToDrugAddictDto(item)));

        return new SuccessResponse<>(drugAddictDtos);
    }

    public SuccessResponse<Object> get(Long id) {
        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));
        return new SuccessResponse<>(convertToDrugAddictDto(drugAddict));
    }
}
