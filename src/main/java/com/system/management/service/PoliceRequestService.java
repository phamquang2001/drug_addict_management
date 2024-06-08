package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.dto.PoliceRequestDto;
import com.system.management.model.entity.Police;
import com.system.management.model.entity.PoliceRequest;
import com.system.management.model.request.police_request.ConfirmPoliceRequestRequest;
import com.system.management.model.request.police_request.GetListPoliceRequestRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.PoliceRequestRepository;
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
public class PoliceRequestService extends BaseCommonService {

    private final PoliceRequestRepository policeRequestRepository;

    public SuccessResponse<Object> getListRequest(GetListPoliceRequestRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        StringBuilder sql = new StringBuilder();

        sql.append(" select *, created_by as txt_created_by, modified_by as txt_modified_by from police_requests where 1 = 1 ");

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

        String status = StringUtils.isNotBlank(request.getStatus()) ? request.getStatus().toUpperCase() : WAIT.name();
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", status);

        sql.append(" order by created_at desc ");

        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size);
        sqlParameterSource.addValue("size", size);

        List<PoliceRequest> policeRequests = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(PoliceRequest.class));

        List<PoliceRequestDto> policeRequestDtos = new ArrayList<>();

        policeRequests.forEach(policeRequest -> policeRequestDtos.add(convertToPoliceRequestDto(policeRequest)));

        return new SuccessResponse<>(policeRequestDtos);
    }

    public SuccessResponse<Object> getRequest(Long id) {
        PoliceRequest policeRequest = policeRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(REQUEST_NOT_EXISTS));

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(policeRequest.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(policeRequest.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(policeRequest.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        return new SuccessResponse<>(convertToPoliceRequestDto(policeRequest));
    }

    public SuccessResponse<Object> confirm(ConfirmPoliceRequestRequest request) {

        PoliceRequest policeRequest = policeRequestRepository
                .findByIdAndStatus(request.getId(), WAIT.name())
                .orElseThrow(() -> new BadRequestException(REQUEST_NOT_EXISTS));

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(policeRequest.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(policeRequest.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(policeRequest.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (request.getStatus().equalsIgnoreCase(ACCEPT.name())) {

            Police police = policeRepository
                    .findByIdAndStatus(policeRequest.getPoliceId(), ACTIVE.name())
                    .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

            police.setAvatar(policeRequest.getAvatar());
            police.setFullName(policeRequest.getFullName());
            police.setGender(policeRequest.getGender());
            police.setDateOfBirth(policeRequest.getDateOfBirth());
            police.setPhoneNumber(policeRequest.getPhoneNumber());
            police.setEmail(policeRequest.getEmail());
            police.setLevel(policeRequest.getLevel());
            police.setRole(policeRequest.getRole());
            police.setCityId(policeRequest.getCityId());
            police.setDistrictId(policeRequest.getDistrictId());
            police.setWardId(policeRequest.getWardId());
            policeRepository.save(police);

            policeRequest.setStatus(ACCEPT.name());

        } else if (request.getStatus().equalsIgnoreCase(REJECT.name())) {

            if (StringUtils.isBlank(request.getReasonRejected())) {
                throw new BadRequestException(REASON_REJECTED_REQUIRED);
            }

            policeRequest.setStatus(REJECT.name());
            policeRequest.setReasonRejected(request.getReasonRejected());

        } else {
            throw new BadRequestException(INVALID_STATUS);
        }

        policeRequestRepository.save(policeRequest);

        return new SuccessResponse<>();
    }
}
