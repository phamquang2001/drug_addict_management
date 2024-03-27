package com.system.management.service;

import com.system.management.model.entity.DrugAddict;
import com.system.management.model.request.drug_addict.GetListDrugAddictRequest;
import com.system.management.model.request.drug_addict.InsertDrugAddictRequest;
import com.system.management.model.request.drug_addict.UpdateDrugAddictRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.CityRepository;
import com.system.management.repository.DistrictRepository;
import com.system.management.repository.DrugAddictRepository;
import com.system.management.repository.WardRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.system.management.utils.constants.ErrorMessage.DRUG_ADDICT_NOT_EXISTS;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugAddictService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final MapSqlParameterSource sqlParameterSource;

    private final DrugAddictRepository drugAddictRepository;

    private final CityRepository cityRepository;

    private final DistrictRepository districtRepository;

    private final WardRepository wardRepository;

    private final FunctionUtils functionUtils;

    private final ModelMapper modelMapper;

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> insert(InsertDrugAddictRequest request) {
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdateDrugAddictRequest request) {
        return null;
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
        return null;
    }

    public SuccessResponse<Object> get(Long id) {
        return null;
    }
}
