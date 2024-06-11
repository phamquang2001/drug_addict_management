package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.dto.WardDto;
import com.system.management.model.entity.Ward;
import com.system.management.model.request.ward.GetListWardRequest;
import com.system.management.model.request.ward.InsertWardRequest;
import com.system.management.model.request.ward.UpdateWardRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ForbiddenException;
import com.system.management.utils.exception.ProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class WardService extends BaseCommonService {

    public SuccessResponse<Object> insert(InsertWardRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login có cấp bậc là phường xã thì không được phép
        if (Objects.equals(loggedAccount.getLevel(), LevelEnums.WARD.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản có thông tin id tỉnh thành phố đơn vị công tác thì chỉ được phép tạo quận huyện trong tỉnh thành phố của mình
        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
        } else {

            // Lấy ra thông tin id tỉnh thành phố mà phuờng xã trực thuộc
            cityId = request.getCityId();

            // Kiểm tra xem tỉnh thành phố có tồn tại và hoạt động
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        // Nếu tài khoản có thông tin id quận huyện đơn vị công tác thì chỉ được phép tạo quận huyện trong quận huyện của mình
        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
        } else {

            // Lấy ra thông tin id quận huyện mà phuờng xã trực thuộc
            districtId = request.getDistrictId();

            // Kiểm tra xem quận huyện có tồn tại và hoạt động
            if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        // Kiểm tra có phường xã nào đang hoạt động mà có mã giống với mã truyền xuống hay không ?
        // Nếu có ném ra lỗi
        if (wardRepository.existsByCodeAndStatusAndDistrictIdAndCityId(request.getCode(), ACTIVE.name(), districtId, cityId)) {
            throw new ProcessException(WARD_EXISTS_WITH_CODE);
        }

        // Khởi tạo bản ghi phường xã mới
        Ward ward = new Ward();
        ward.setCityId(cityId);                                                         // ID tỉnh thành phố trực thuộc
        ward.setDistrictId(districtId);                                                 // ID quận huyện trực thuộc
        ward.setStatus(ACTIVE.name());                                                  // Trạng thái Hoạt động (ACTIVE)
        ward.setCode(request.getCode());                                                // Mã phường xã
        ward.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));         // Tên phường xã
        ward.setUnsignedName(FunctionUtils.normalizeAndLowercase(ward.getFullName()));  // Tên phường xã viết thường không dấu dùng để phục vụ tìm kiếm địa chính

        // Trả về thành công kèm thông tin phường xã vừa tạo
        return new SuccessResponse<>(modelMapper.map(wardRepository.save(ward), WardDto.class));
    }

    public SuccessResponse<Object> update(UpdateWardRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login có cấp bậc là phường xã thì không được phép
        if (Objects.equals(loggedAccount.getLevel(), LevelEnums.WARD.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong wards id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        Long wardId = request.getId();
        Ward ward = wardRepository
                .findByIdAndStatus(wardId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(WARD_NOT_EXISTS));

        // Nếu tài khoản có thông tin id tỉnh thành phố đơn vị công tác thì chỉ được phép cập nhật phường xã trong tỉnh thành phố của mình
        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
            if (!cityId.equals(ward.getCityId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else { // Kiểm tra xem tỉnh thành phố theo id truyền xuống có tồn tại và hoạt động
            cityId = request.getCityId();
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        // Nếu tài khoản có thông tin id quận huyện đơn vị công tác thì chỉ được phép cập nhật phường xã trong quận huyện của mình
        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
            if (!districtId.equals(ward.getDistrictId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else { // Kiểm tra xem quận huyện theo id truyền xuống có tồn tại và hoạt động
            districtId = request.getDistrictId();
            if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        // Kiểm tra có phường xã nào đang hoạt động mà có mã giống với mã truyền xuống hay không ?
        // Nếu có ném ra lỗi
        if (wardRepository.existsByCodeAndStatusAndIdNotAndDistrictIdAndCityId(request.getCode(), ACTIVE.name(), wardId, districtId, cityId)) {
            throw new ProcessException(WARD_EXISTS_WITH_CODE);
        }

        // Cập nhật thông tin phường xã
        ward.setCityId(cityId);                                                         // ID tỉnh thành phố trực thuộc
        ward.setDistrictId(districtId);                                                 // ID quận huyện trực thuộc
        ward.setCode(request.getCode());                                                // Mã phường xã
        ward.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));         // Tên phường xã
        ward.setUnsignedName(FunctionUtils.normalizeAndLowercase(ward.getFullName()));  // Tên phường xã viết thường không dấu dùng để phục vụ tìm kiếm địa chính

        // Trả về thành công kèm thông tin phường xã vừa cập nhật
        return new SuccessResponse<>(modelMapper.map(wardRepository.save(ward), WardDto.class));
    }

    public SuccessResponse<Object> delete(Long id) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login có cấp bậc là phường xã thì không được phép
        if (Objects.equals(loggedAccount.getLevel(), LevelEnums.WARD.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong wards theo id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        Ward ward = wardRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(WARD_NOT_EXISTS));

        // Tài khoản đang login chỉ được phép xóa phường xã trực thuộc tỉnh thành phố đơn vị mình công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(ward.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tài khoản đang login chỉ được phép xóa phường xã trực thuộc quận huyện đơn vị mình công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(ward.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Cập nhật trạng thái bản ghi quận huyện là DELETED
        ward.setStatus(DELETED.name());
        wardRepository.save(ward);

        // Trả về thành công
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListWardRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Khởi tạo query lấy ra danh sách phường xã
        StringBuilder sql = new StringBuilder();
        sql.append(" select id, code, full_name, city_id, district_id, status from wards where 1 = 1 ");

        // Tài khoản đang login chỉ được phép tìm kiếm phường xã trực thuộc tỉnh thành phố đơn vị mình công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố trực thuộc
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố đơn vị công tác của cảnh sát
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());

        } else if (!FunctionUtils.isNullOrZero(request.getCityId())) { // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố trực thuộc
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Tài khoản đang login chỉ được phép tìm kiếm phường xã trực thuộc quận huyện đơn vị mình công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện trực thuộc
            sql.append(" and district_id = :district_id ");

            // Set tham số trong query là id quận huyện đơn vị công tác của cảnh sát
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());

        } else if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện trực thuộc
            sql.append(" and district_id = :district_id ");

            // Set tham số trong query là id quận huyện muốn tìm kiếm
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        // Nếu có dữ liệu id phường xã muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id phường xã muốn tìm kiếm
            sql.append(" and id = :ward_id ");

            // Set tham số trong query là id phường xã muốn tìm kiếm
            sqlParameterSource.addValue("ward_id", loggedAccount.getWardId());
        }

        // Nếu có dữ liệu mã phường xã muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getCode())) {

            // Cộng chuỗi query thêm tìm kiếm theo mã phường xã muốn tìm kiếm
            sql.append(" and code = :code ");

            // Set tham số trong query là mã phường xã muốn tìm kiếm
            sqlParameterSource.addValue("code", request.getCode());
        }

        // Nếu có dữ liệu tên phường xã muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getFullName())) {

            // Cộng chuỗi query thêm tìm kiếm theo tên phường xã muốn tìm kiếm
            sql.append(" and unsigned_name like concat('%', :name, '%') ");

            // Set tham số trong query là tên phường xã muốn tìm kiếm
            sqlParameterSource.addValue("name", FunctionUtils.normalizeAndLowercase(request.getFullName()));
        }

        // Nếu không có dữ liệu trạng thái muốn tìm kiếm truyền xuống thì mặc định lấy Hoạt động (ACTIVE)
        String status = request.getStatus();
        if (StringUtils.isBlank(status)) status = ACTIVE.name();

        // Set tham số trong query là trạng thái bản ghi phường xã muốn tìm kiếm
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", status.toUpperCase());

        // Sắp xếp theo mã phường xã
        sql.append(" order by code ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng WardDto
        List<WardDto> wards = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(WardDto.class));

        // Duyệt từng phân tử trong districts và set thêm thông tin tỉnh thành phố, quận huyện trực thuộc
        wards.forEach(item -> {
            item.setCity(findCityByIdWithoutAuditor(item.getCityId()));
            item.setDistrict(findDistrictByIdWithoutAuditor(item.getDistrictId()));
        });

        // Trả về thành công kèm danh sách đối tượng WardDto
        return new SuccessResponse<>(wards);
    }
}
