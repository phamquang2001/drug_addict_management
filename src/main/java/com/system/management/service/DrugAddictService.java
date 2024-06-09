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
import com.system.management.utils.exception.ForbiddenException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
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

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Long permanentCityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            permanentCityId = loggedAccount.getCityId();
        } else {
            permanentCityId = request.getPermanentCityId();
            if (!cityRepository.existsByIdAndStatus(permanentCityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        Long permanentDistrictId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            permanentDistrictId = loggedAccount.getDistrictId();
        } else {
            permanentDistrictId = request.getPermanentDistrictId();
            if (!districtRepository.existsByIdAndStatus(permanentDistrictId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        Long permanentWardId = null;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            permanentWardId = loggedAccount.getWardId();
        } else if (!FunctionUtils.isNullOrZero(request.getPermanentWardId())) {
            permanentWardId = request.getPermanentWardId();
            if (!wardRepository.existsByIdAndStatus(permanentWardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
        }

        String permanentAddressDetail = StringUtils.isBlank(request.getPermanentAddressDetail()) ? "" : request.getPermanentAddressDetail().trim();

        if (drugAddictRepository.existsByIdentifyNumberAndStatus(request.getIdentifyNumber(), StatusEnums.ACTIVE.name())) {
            throw new BadRequestException(INVALID_IDENTIFY_NUMBER);
        }

        Long policeId = request.getPoliceId();
        if (!FunctionUtils.isNullOrZero(policeId) && !policeRepository.existsByIdAndStatus(policeId, ACTIVE.name())) {
            throw new BadRequestException(POLICE_NOT_EXISTS);
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
        drugAddict.setPoliceId(policeId);
        drugAddict.setTreatmentPlaceId(treatmentPlaceId);
        drugAddict.setPermanentWardId(permanentWardId);
        drugAddict.setPermanentDistrictId(permanentDistrictId);
        drugAddict.setPermanentCityId(permanentCityId);
        drugAddict.setPermanentAddressDetail(permanentAddressDetail.trim());
        drugAddict.setIsAtPermanent(request.getIsAtPermanent());
        drugAddict.setStatus(ACTIVE.name());

        if (StringUtils.isNotBlank(request.getAvatar())) {
            drugAddict.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
        }

        if (Boolean.TRUE.equals(request.getIsAtPermanent())) {

            drugAddict.setCurrentWardId(permanentWardId);
            drugAddict.setCurrentDistrictId(permanentDistrictId);
            drugAddict.setCurrentCityId(permanentCityId);
            drugAddict.setCurrentAddressDetail(permanentAddressDetail);

        } else if (!FunctionUtils.isNullOrZero(request.getCurrentCityId())) {

            Long currentWardId = request.getCurrentWardId();
            Long currentDistrictId = request.getCurrentDistrictId();
            Long currentCityId = request.getCurrentCityId();
            String currentAddressDetail = StringUtils.isBlank(request.getCurrentAddressDetail()) ? "" : request.getCurrentAddressDetail().trim();

            if (FunctionUtils.isNullOrZero(currentCityId)
                    || !cityRepository.existsByIdAndStatus(currentCityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }

            if (FunctionUtils.isNullOrZero(currentDistrictId)
                    || !districtRepository.existsByIdAndStatus(currentDistrictId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
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

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)
                && !Objects.equals(loggedAccount.getId(), drugAddict.getPoliceId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Long permanentCityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            permanentCityId = loggedAccount.getCityId();
            if (!permanentCityId.equals(drugAddict.getPermanentCityId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {
            permanentCityId = request.getPermanentCityId();
            if (!cityRepository.existsByIdAndStatus(permanentCityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        Long permanentDistrictId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            permanentDistrictId = loggedAccount.getDistrictId();
            if (!permanentDistrictId.equals(drugAddict.getPermanentDistrictId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {
            permanentDistrictId = request.getPermanentDistrictId();
            if (!districtRepository.existsByIdAndStatus(permanentDistrictId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        Long permanentWardId = null;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            permanentWardId = loggedAccount.getWardId();
            if (!permanentWardId.equals(drugAddict.getPermanentWardId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else if (!FunctionUtils.isNullOrZero(request.getPermanentWardId())) {
            permanentWardId = request.getPermanentWardId();
            if (!wardRepository.existsByIdAndStatus(permanentWardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
        }

        String permanentAddressDetail = request.getPermanentAddressDetail();

        Long treatmentPlaceId = request.getTreatmentPlaceId();
        if (!FunctionUtils.isNullOrZero(treatmentPlaceId)
                && !treatmentPlaceRepository.existsByIdAndStatus(treatmentPlaceId, ACTIVE.name())) {
            throw new BadRequestException(TREATMENT_PLACE_NOT_EXISTS);
        }

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        SuccessResponse<Object> response;

        if (Objects.equals(loggedAccount.getRole(), RoleEnums.POLICE.value)) {

            DrugAddictRequest drugAddictRequest = drugAddictRequestRepository
                    .findByDrugAddictIdAndStatus(drugAddict.getId(), StatusEnums.WAIT.name())
                    .orElse(new DrugAddictRequest());

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
            drugAddictRequest.setStatus(StatusEnums.WAIT.name());

            if (StringUtils.isNotBlank(request.getAvatar())) {
                drugAddictRequest.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
            } else {
                drugAddictRequest.setAvatar(drugAddict.getAvatar());
            }

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

            if (StringUtils.isNotBlank(request.getAvatar())) {
                drugAddict.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
            }

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

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(drugAddict.getPermanentCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(drugAddict.getPermanentDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(drugAddict.getPermanentWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        drugAddict.setStatus(DELETED.name());
        drugAddictRepository.save(drugAddict);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListDrugAddictRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (Objects.equals(loggedAccount.getRole(), RoleEnums.POLICE.value)) {
            request.setPoliceId(loggedAccount.getId());
        }

        StringBuilder sql = new StringBuilder();

        sql.append(" select da.*, da.created_by as txt_created_by, da.modified_by as txt_modified_by ");
        sql.append(" from drug_addicts da left join polices p on da.police_id = p.id where 1 = 1 ");

        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {
            sql.append(" and da.identify_number like concat('%', :da_identify_number, '%') ");
            sqlParameterSource.addValue("da_identify_number", request.getIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and da.full_name like concat('%', :da_full_name, '%') ");
            sqlParameterSource.addValue("da_full_name", request.getFullName());
        }

        if (!FunctionUtils.isNullOrZero(request.getSupervisorStatus())) {
            if (request.getSupervisorStatus() == 0) {
                sql.append(" and da.police_id is not null ");
            } else if (request.getSupervisorStatus() == 1) {
                sql.append(" and da.police_id is null ");
            }
        }

        if (!FunctionUtils.isNullOrZero(request.getSupervisorLevel())) {
            sql.append(" and p.level = :level ");
            sqlParameterSource.addValue("level", request.getSupervisorLevel());
        }

        if (StringUtils.isNotBlank(request.getSupervisorIdentifyNumber())) {
            sql.append(" and da.identify_number like concat('%', :p_identify_number, '%') ");
            sqlParameterSource.addValue("p_identify_number", request.getSupervisorIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getSupervisorFullName())) {
            sql.append(" and p.full_name like concat('%', :p_full_name, '%') ");
            sqlParameterSource.addValue("p_full_name", request.getSupervisorFullName());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            sql.append(" and da.permanent_city_id = :city_id ");
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());
        } else if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and da.permanent_city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            sql.append(" and da.permanent_district_id = :district_id ");
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());
        } else if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and da.permanent_district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            sql.append(" and da.permanent_ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", loggedAccount.getWardId());
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and da.permanent_ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        if (!FunctionUtils.isNullOrZero(request.getTreatmentPlaceId())) {
            sql.append(" and da.treatment_place_id = :treatment_place_id ");
            sqlParameterSource.addValue("treatment_place_id", request.getTreatmentPlaceId());
        }

        if (!FunctionUtils.isNullOrZero(request.getPoliceId())) {
            sql.append(" and da.police_id = :police_id ");
            sqlParameterSource.addValue("police_id", request.getPoliceId());
        }

        sql.append(" and da.status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        sql.append(" order by da.created_at desc ");

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

    public SuccessResponse<Object> get(Long id) {

        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)
                && !Objects.equals(loggedAccount.getId(), drugAddict.getPoliceId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(drugAddict.getPermanentCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(drugAddict.getPermanentDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(drugAddict.getPermanentWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        return new SuccessResponse<>(convertToDrugAddictDto(drugAddict));
    }
}
