package com.system.management.service;

import com.system.management.model.dto.DistrictDto;
import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.District;
import com.system.management.model.request.district.GetListDistrictRequest;
import com.system.management.model.request.district.InsertDistrictRequest;
import com.system.management.model.request.district.UpdateDistrictRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
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
public class DistrictService extends BaseCommonService {

    public SuccessResponse<Object> insert(InsertDistrictRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản đang login có cấp bậc là quận huyện hoặc phường xã thì không được phép
        if (loggedAccount.getLevel() > LevelEnums.CITY.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản có thông tin id tỉnh thành phố đơn vị công tác thì chỉ được phép tạo quận huyện trong tỉnh thành phố của mình
        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
        } else {

            // Lấy ra thông tin id tỉnh thành phố mà quận huyện trực thuộc
            cityId = request.getCityId();

            // Kiểm tra xem tỉnh thành phố có tồn tại và hoạt động
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        // Kiểm tra có quận huyện nào đang hoạt động mà có mã giống với mã truyền xuống hay không ?
        // Nếu có ném ra lỗi
        if (districtRepository.existsByCodeAndStatusAndCityId(request.getCode(), ACTIVE.name(), cityId)) {
            throw new ProcessException(DISTRICT_EXISTS_WITH_CODE);
        }

        // Khởi tạo bản ghi quận huyện mới
        District district = new District();
        district.setCityId(cityId);                                                             // ID tỉnh thành phố trực thuộc
        district.setStatus(ACTIVE.name());                                                      // Trạng thái Hoạt động (ACTIVE)
        district.setCode(request.getCode());                                                    // Mã quận huyện
        district.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));             // Tên quận huyện
        district.setUnsignedName(FunctionUtils.normalizeAndLowercase(district.getFullName()));  // Tên quận huyện viết thường không dấu dùng để phục vụ tìm kiếm địa chính

        // Trả về thành công kèm thông tin quận huyện vừa tạo
        return new SuccessResponse<>(modelMapper.map(districtRepository.save(district), DistrictDto.class));
    }

    public SuccessResponse<Object> update(UpdateDistrictRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản đang login có cấp bậc là quận huyện hoặc phường xã thì không được phép
        if (loggedAccount.getLevel() > LevelEnums.CITY.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong districts id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        Long districtId = request.getId();
        District district = districtRepository
                .findByIdAndStatus(districtId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DISTRICT_NOT_EXISTS));

        // Nếu tài khoản có thông tin id tỉnh thành phố đơn vị công tác thì chỉ được phép cập nhật quận huyện trong tỉnh thành phố của mình
        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
            if (!cityId.equals(district.getCityId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else { // Kiểm tra xem tỉnh thành phố theo id truyền xuống có tồn tại và hoạt động
            cityId = request.getCityId();
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        // Kiểm tra có quận huyện nào đang hoạt động mà có mã giống với mã truyền xuống hay không ?
        // Nếu có ném ra lỗi
        if (districtRepository.existsByCodeAndStatusAndIdNotAndCityId(request.getCode(), ACTIVE.name(), districtId, cityId)) {
            throw new ProcessException(DISTRICT_EXISTS_WITH_CODE);
        }

        // Cập nhật thông tin quận huyện
        district.setCityId(cityId);                                                             // ID tỉnh thành phố trực thuộc
        district.setCode(request.getCode());                                                    // Mã quận huyện
        district.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));             // Tên quận huyện
        district.setUnsignedName(FunctionUtils.normalizeAndLowercase(district.getFullName()));  // Tên quận huyện viết thường không dấu dùng để phục vụ tìm kiếm địa chính

        // Trả về thành công kèm thông tin quận huyện vừa cập nhật
        return new SuccessResponse<>(modelMapper.map(districtRepository.save(district), DistrictDto.class));
    }

    public SuccessResponse<Object> delete(Long id) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản đang login có cấp bậc là quận huyện hoặc phường xã thì không được phép
        if (loggedAccount.getLevel() > LevelEnums.CITY.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong districts theo id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        District district = districtRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DISTRICT_NOT_EXISTS));

        // Tài khoản đang login chỉ được phép xóa quận huyện trực thuộc tỉnh thành phố đơn vị mình công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(district.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Cập nhật trạng thái bản ghi quận huyện là DELETED
        district.setStatus(DELETED.name());
        districtRepository.save(district);

        // Trả về thành công
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListDistrictRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Khởi tạo query lấy ra danh sách quận huyện
        StringBuilder sql = new StringBuilder();
        sql.append(" select id, code, full_name, city_id, status from districts where 1 = 1 ");

        // Tài khoản đang login chỉ được phép tìm kiếm quận huyện trực thuộc tỉnh thành phố đơn vị mình công tác
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố quận huyện trực thuộc
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố đơn vị công tác của cảnh sát
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());

        } else if (!FunctionUtils.isNullOrZero(request.getCityId())) { // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố quận huyện trực thuộc
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu có dữ liệu id quận huyện muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện muốn tìm kiếm
            sql.append(" and id = :district_id ");

            // Set tham số trong query là id quận huyện muốn tìm kiếm
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());
        }

        // Nếu có dữ liệu mã quận huyện muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getCode())) {

            // Cộng chuỗi query thêm tìm kiếm theo mã quận huyện muốn tìm kiếm
            sql.append(" and code = :code ");

            // Set tham số trong query là mã quận huyện muốn tìm kiếm
            sqlParameterSource.addValue("code", request.getCode());
        }

        // Nếu có dữ liệu tên quận huyện muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getFullName())) {

            // Cộng chuỗi query thêm tìm kiếm theo tên quận huyện muốn tìm kiếm
            sql.append(" and unsigned_name like concat('%', :name, '%') ");

            // Set tham số trong query là tên quận huyện muốn tìm kiếm
            sqlParameterSource.addValue("name", FunctionUtils.normalizeAndLowercase(request.getFullName()));
        }

        // Nếu không có dữ liệu trạng thái muốn tìm kiếm truyền xuống thì mặc định lấy Hoạt động (ACTIVE)
        String status = request.getStatus();
        if (StringUtils.isBlank(status)) status = ACTIVE.name();

        // Set tham số trong query là trạng thái bản ghi quận huyện muốn tìm kiếm
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", status.toUpperCase());

        // Sắp xếp theo mã quận huyện
        sql.append(" order by code ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng DistrictDto
        List<DistrictDto> districts = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(DistrictDto.class));

        // Duyệt từng phân tử trong districts và set thêm thông tin tỉnh thành phố trực thuộc
        districts.forEach(item -> item.setCity(findCityByIdWithoutAuditor(item.getCityId())));

        // Trả về thành công kèm danh sách đối tượng DistrictDto
        return new SuccessResponse<>(districts);
    }
}
