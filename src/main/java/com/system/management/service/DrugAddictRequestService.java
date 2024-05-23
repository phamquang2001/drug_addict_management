package com.system.management.service;

import com.system.management.model.dto.DrugAddictRequestDto;
import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.DrugAddict;
import com.system.management.model.entity.DrugAddictRequest;
import com.system.management.model.request.drug_addict_request.ConfirmDrugAddictRequestRequest;
import com.system.management.model.request.drug_addict_request.GetListDrugAddictRequestRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.DrugAddictRepository;
import com.system.management.repository.DrugAddictRequestRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ForbiddenException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.StatusEnums.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugAddictRequestService extends BaseCommonService {

    private final DrugAddictRepository drugAddictRepository;

    private final DrugAddictRequestRepository drugAddictRequestRepository;

    public SuccessResponse<Object> getListRequest(GetListDrugAddictRequestRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        StringBuilder sql = new StringBuilder();

        sql.append(" select *, created_by as txt_created_by, modified_by as txt_modified_by from drug_addict_requests where 1 = 1 ");

        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {
            sql.append(" and identify_number like concat('%', :identify_number, '%') ");
            sqlParameterSource.addValue("identify_number", request.getIdentifyNumber());
        }

        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and full_name like concat('%', :full_name, '%') ");
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        if (request.getStartDate() != null) {
            sql.append(" and DATE (created_at) >= DATE (:start_date) ");
            sqlParameterSource.addValue("start_date", request.getStartDate());
        }

        if (request.getEndDate() != null) {
            sql.append(" and DATE (created_at) <= DATE (:end_date) ");
            sqlParameterSource.addValue("end_date", request.getEndDate());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            sql.append(" and permanent_city_id = :city_id ");
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());
        } else if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and permanent_city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            sql.append(" and permanent_district_id = :district_id ");
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());
        } else if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and permanent_district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            sql.append(" and permanent_ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", loggedAccount.getWardId());
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and permanent_ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        String status = StringUtils.isNotBlank(request.getStatus()) ? request.getStatus().toUpperCase() : WAIT.name();
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", status);

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 10 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<DrugAddictRequest> drugAddictRequests = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(DrugAddictRequest.class));

        List<DrugAddictRequestDto> drugAddictRequestDtos = new ArrayList<>();

        drugAddictRequests.forEach(drugAddictRequest -> drugAddictRequestDtos.add(convertToDrugAddictRequestDto(drugAddictRequest)));

        return new SuccessResponse<>(drugAddictRequestDtos);
    }

    public SuccessResponse<Object> getRequest(Long id) {

        DrugAddictRequest drugAddictRequest = drugAddictRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(REQUEST_NOT_EXISTS));

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(drugAddictRequest.getPermanentCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(drugAddictRequest.getPermanentDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(drugAddictRequest.getPermanentWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        return new SuccessResponse<>(convertToDrugAddictRequestDto(drugAddictRequest));
    }

    public SuccessResponse<Object> confirm(ConfirmDrugAddictRequestRequest request) {

        DrugAddictRequest drugAddictRequest = drugAddictRequestRepository
                .findByIdAndStatus(request.getId(), WAIT.name())
                .orElseThrow(() -> new BadRequestException(REQUEST_NOT_EXISTS));

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(drugAddictRequest.getPermanentCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(drugAddictRequest.getPermanentDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(drugAddictRequest.getPermanentWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (request.getStatus().equalsIgnoreCase(ACCEPT.name())) {

            DrugAddict drugAddict = drugAddictRepository
                    .findByIdAndStatus(drugAddictRequest.getDrugAddictId(), ACTIVE.name())
                    .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

            drugAddict.setAvatar(drugAddictRequest.getAvatar());
            drugAddict.setFullName(drugAddictRequest.getFullName());
            drugAddict.setGender(drugAddictRequest.getGender());
            drugAddict.setDateOfBirth(drugAddictRequest.getDateOfBirth());
            drugAddict.setPhoneNumber(drugAddictRequest.getPhoneNumber());
            drugAddict.setEmail(drugAddictRequest.getEmail());
            drugAddict.setTreatmentPlaceId(drugAddictRequest.getTreatmentPlaceId());
            drugAddict.setPermanentWardId(drugAddictRequest.getPermanentWardId());
            drugAddict.setPermanentDistrictId(drugAddictRequest.getPermanentDistrictId());
            drugAddict.setPermanentCityId(drugAddictRequest.getPermanentCityId());
            drugAddict.setPermanentAddressDetail(drugAddictRequest.getPermanentAddressDetail());
            drugAddict.setIsAtPermanent(drugAddictRequest.getIsAtPermanent());
            drugAddict.setCurrentWardId(drugAddictRequest.getCurrentWardId());
            drugAddict.setCurrentDistrictId(drugAddictRequest.getCurrentDistrictId());
            drugAddict.setCurrentCityId(drugAddictRequest.getCurrentCityId());
            drugAddict.setCurrentAddressDetail(drugAddictRequest.getCurrentAddressDetail());
            drugAddict.setIsAtPermanent(drugAddictRequest.getIsAtPermanent());
            drugAddictRepository.save(drugAddict);

            drugAddictRequest.setStatus(ACCEPT.name());

        } else if (request.getStatus().equalsIgnoreCase(REJECT.name())) {

            if (StringUtils.isBlank(request.getReasonRejected())) {
                throw new BadRequestException(REASON_REJECTED_REQUIRED);
            }

            drugAddictRequest.setStatus(REJECT.name());
            drugAddictRequest.setReasonRejected(request.getReasonRejected());

        } else {
            throw new BadRequestException(INVALID_STATUS);
        }

        drugAddictRequestRepository.save(drugAddictRequest);

        return new SuccessResponse<>();
    }
}
