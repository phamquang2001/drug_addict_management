package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.Police;
import com.system.management.model.request.police.GetListPoliceRequest;
import com.system.management.model.request.police.InsertPoliceRequest;
import com.system.management.model.request.police.UpdatePoliceRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.GenderEnums;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.enums.StatusEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ForbiddenException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.AssignStatusEnums.UN_ASSIGN;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoliceService extends BaseCommonService {

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> insert(InsertPoliceRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
        } else {
            cityId = request.getCityId();
        }

        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
        } else {
            districtId = request.getDistrictId();
        }

        Long wardId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            wardId = loggedAccount.getWardId();
        } else {
            wardId = request.getWardId();
        }

        if (policeRepository.existsByIdentifyNumberAndStatus(request.getIdentifyNumber(), StatusEnums.ACTIVE.name())) {
            throw new BadRequestException(INVALID_IDENTIFY_NUMBER);
        }

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        LevelEnums level = LevelEnums.dict.get(request.getLevel());
        if (level == null) {
            throw new BadRequestException(INVALID_LEVEL);
        }

        String password = FunctionUtils.generatePassword();

        Police police = new Police();
        police.setIdentifyNumber(request.getIdentifyNumber());
        police.setPassword(passwordEncoder.encode(password));
        police.setFullName(request.getFullName());
        police.setGender(gender.value);
        police.setDateOfBirth(request.getDateOfBirth());
        police.setPhoneNumber(request.getPhoneNumber());
        police.setEmail(request.getEmail());
        police.setLevel(level.value);
        police.setRole(RoleEnums.POLICE.value);
        police.setStatus(ACTIVE.name());
        police.setAssignStatus(UN_ASSIGN.getValue());

        if (StringUtils.isNotBlank(request.getAvatar())) {
            police.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
        }

        if (police.getLevel() > LevelEnums.CENTRAL.value
                && (FunctionUtils.isNullOrZero(cityId) || !cityRepository.existsByIdAndStatus(cityId, ACTIVE.name()))) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        } else {
            police.setCityId(cityId);
        }

        if (police.getLevel() > LevelEnums.CENTRAL.value) {
            if (FunctionUtils.isNullOrZero(cityId)
                    || !cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
            police.setCityId(cityId);
        }

        if (police.getLevel() > LevelEnums.CITY.value) {
            if (FunctionUtils.isNullOrZero(districtId)
                    || !districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
            police.setDistrictId(districtId);
        }

        if (police.getLevel() > LevelEnums.DISTRICT.value) {
            if (FunctionUtils.isNullOrZero(wardId)
                    || !wardRepository.existsByIdAndStatus(wardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
            police.setWardId(wardId);
        }

        police = policeRepository.save(police);
        PoliceDto policeDto = convertToPoliceDto(police);

        emailService.sendMailAccountCreated(policeDto, password);

        return new SuccessResponse<>(policeDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdatePoliceRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Police police = policeRepository
                .findByIdAndStatus(request.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        Police oldSheriff = getSheriff(police);

        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
            if (!cityId.equals(police.getCityId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {
            cityId = request.getCityId();
        }

        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
            if (!districtId.equals(police.getDistrictId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {
            districtId = request.getDistrictId();
        }

        Long wardId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            wardId = loggedAccount.getWardId();
            if (!wardId.equals(police.getWardId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {
            wardId = request.getWardId();
        }

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        RoleEnums role = RoleEnums.dict.get(request.getRole());
        if (role == null) {
            throw new BadRequestException(INVALID_ROLE);
        }

        LevelEnums level = LevelEnums.dict.get(request.getLevel());
        if (level == null) {
            throw new BadRequestException(INVALID_LEVEL);
        }

        if (Objects.equals(police.getIdentifyNumber(), oldSheriff.getIdentifyNumber())) {

            if (Objects.equals(role.value, RoleEnums.POLICE.value)) {
                throw new BadRequestException(REQUIRED_NEW_SHERIFF);
            }

            if (!Objects.equals(oldSheriff.getCityId(), cityId)
                    || !Objects.equals(oldSheriff.getDistrictId(), districtId)
                    || !Objects.equals(oldSheriff.getWardId(), wardId)) {
                throw new BadRequestException(NOT_ALLOW_CHANGE_CADASTRAL_SHERIFF);
            }
        }

        police.setFullName(request.getFullName());
        police.setGender(gender.value);
        police.setDateOfBirth(request.getDateOfBirth());
        police.setPhoneNumber(request.getPhoneNumber());
        police.setEmail(request.getEmail());
        police.setRole(role.value);
        police.setLevel(level.value);
        police.setCityId(null);
        police.setDistrictId(null);
        police.setWardId(null);

        if (StringUtils.isNotBlank(request.getAvatar())) {
            police.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
        }

        if (police.getLevel() > LevelEnums.CENTRAL.value) {
            if (FunctionUtils.isNullOrZero(cityId)
                    || !cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
            police.setCityId(cityId);
        }

        if (police.getLevel() > LevelEnums.CITY.value) {
            if (FunctionUtils.isNullOrZero(districtId)
                    || !districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
            police.setDistrictId(districtId);
        }

        if (police.getLevel() > LevelEnums.DISTRICT.value) {
            if (FunctionUtils.isNullOrZero(wardId)
                    || !wardRepository.existsByIdAndStatus(wardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
            police.setWardId(wardId);
        }

        police = policeRepository.save(police);

        if (Objects.equals(role.value, RoleEnums.SHERIFF.value)) {
            policeRepository.updateRoleOldSheriff(RoleEnums.POLICE.value, oldSheriff.getId());
        }

        return new SuccessResponse<>(convertToPoliceDto(police));
    }

    public SuccessResponse<Object> delete(Long id) {

        PoliceDto loggedAccount = getLoggedAccount();
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        Police police = policeRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(police.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(police.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(police.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        police.setStatus(DELETED.name());
        policeRepository.save(police);
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListPoliceRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Khởi tạo query lấy ra danh sách id cảnh sát thuộc đơn vị công tác của tài khoản đang login
        StringBuilder sql = new StringBuilder();
        sql.append(" select id from polices where 1 = 1 ");

        // Nếu tài khoản có id tỉnh thành phố đơn vị công tác
        // => Chỉ được phép lấy ra các cảnh sát có cùng id tỉnh thành phố đơn vị công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());
        }

        // Nếu tài khoản có id quận huyện đơn vị công tác
        // => Chỉ được phép lấy ra các cảnh sát có cùng id quận huyện đơn vị công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            sql.append(" and district_id = :district_id ");
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());
        }

        // Nếu tài khoản có id phường xã đơn vị công tác
        // => Chỉ được phép lấy ra các cảnh sát có cùng id phường xã đơn vị công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            sql.append(" and ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", loggedAccount.getWardId());
        }

        // Thực thi query lấy ra danh sách id cảnh sát được phép xem
        List<Long> ids = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, SingleColumnRowMapper.newInstance(Long.class));

        // Khởi tạo query lấy ra danh sách id cảnh sát được phân công hỗ trợ địa chính đơn vị công tác của tài khoản login
        sql = new StringBuilder();
        sql.append(" select police_id from police_requests where 1 = 1 ");

        // Nếu tài khoản có id tỉnh thành phố đơn vị công tác
        // => Chỉ được phép lấy ra các cảnh sát được phân công hỗ trợ tỉnh thành phố đơn vị công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());
        }

        // Nếu tài khoản có id quận huyện đơn vị công tác
        // => Chỉ được phép lấy ra các cảnh sát được phân công hỗ trợ quận huyện đơn vị công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            sql.append(" and district_id = :district_id ");
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());
        }

        // Nếu tài khoản có id phường xã đơn vị công tác
        // => Chỉ được phép lấy ra các cảnh sát được phân công hỗ trợ phường xã đơn vị công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            sql.append(" and ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", loggedAccount.getWardId());
        }

        // Thực thi query lấy ra bổ sung danh sách id cảnh sát được phép xem
        ids.addAll(namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, SingleColumnRowMapper.newInstance(Long.class)));

        // Khởi tạo query lấy ra danh sách cảnh sát được phép xem theo danh sách id đã lấy ra
        sql = new StringBuilder();
        sql.append(" select *, created_by as txt_created_by, modified_by as txt_modified_by from polices where id in (:ids) ");
        sqlParameterSource.addValue("ids", ids);

        // Nếu có dữ liệu tìm kiếm theo số CCCD
        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {
            sql.append(" and identify_number like concat('%', :identify_number, '%') ");
            sqlParameterSource.addValue("identify_number", request.getIdentifyNumber());
        }

        // Nếu có dữ liệu tìm kiếm theo họ tên
        if (StringUtils.isNotBlank(request.getFullName())) {
            sql.append(" and full_name like concat('%', :full_name, '%') ");
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        // Nếu có dữ liệu tìm kiếm theo vai trò
        if (!FunctionUtils.isNullOrZero(request.getRole())) {
            sql.append(" and role = :role ");
            sqlParameterSource.addValue("role", request.getRole());
        }

        // Nếu có dữ liệu tìm kiếm theo cấp bậc
        if (!FunctionUtils.isNullOrZero(request.getLevel())) {
            sql.append(" and level = :level ");
            sqlParameterSource.addValue("level", request.getLevel());
        }

        // Nếu có dữ liệu tìm kiếm theo tỉnh thành phố đơn vị công tác
        if (!FunctionUtils.isNullOrZero(request.getCityId())) {
            sql.append(" and city_id = :city_id ");
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu có dữ liệu tìm kiếm theo quận huyện đơn vị công tác
        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {
            sql.append(" and district_id = :district_id ");
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        // Nếu có dữ liệu tìm kiếm theo phường xã đơn vị công tác
        if (!FunctionUtils.isNullOrZero(request.getWardId())) {
            sql.append(" and ward_id = :ward_id ");
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        // Nếu có dữ liệu tìm kiếm theo trạng thái phân công
        if (request.getAssignStatus() != null) {
            sql.append(" and assign_status = :assign_status ");
            sqlParameterSource.addValue("assign_status", request.getAssignStatus());
        }

        // Mặc định lấy trạng thái Hoạt động
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Sắp xếp theo ngày tạo từ mới nhất đến cũ nhất
        sql.append(" order by created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng Police
        List<Police> polices = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(Police.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng PoliceDto
        List<PoliceDto> policeDtos = new ArrayList<>();

        // Duyệt từng phần tử của polices và convert sang PoliceDto rồi thêm vào danh sách policeDtos
        polices.forEach(police -> policeDtos.add(convertToPoliceDto(police)));

        // Trả về thành công kèm danh sách đối tượng PoliceDto
        return new SuccessResponse<>(policeDtos);
    }

    public SuccessResponse<Object> get(Long id) {
        Police police = policeRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        PoliceDto loggedAccount = getLoggedAccount();

        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(police.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(police.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(police.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        return new SuccessResponse<>(convertToPoliceDto(police));
    }
}
