package com.system.management.service;

import com.system.management.model.dto.CityDto;
import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.City;
import com.system.management.model.request.city.GetListCityRequest;
import com.system.management.model.request.city.InsertCityRequest;
import com.system.management.model.request.city.UpdateCityRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
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
public class CityService extends BaseCommonService {

    public SuccessResponse<Object> insert(InsertCityRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản đang login có cấp bậc là tỉnh thành phố, quận huyện hoặc phường xã thì không được phép
        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Kiểm tra có tỉnh thành phố nào đang hoạt động mà có mã giống với mã truyền xuống hay không ?
        // Nếu có ném ra lỗi
        if (cityRepository.existsByCodeAndStatus(request.getCode(), ACTIVE.name())) {
            throw new ProcessException(CITY_EXISTS_WITH_CODE);
        }

        // Khởi tạo bản ghi tỉnh thành phố mới
        City city = new City();
        city.setStatus(ACTIVE.name());                                                      // Trạng thái Hoạt động (ACTIVE)
        city.setCode(request.getCode());                                                    // Mã tỉnh thành phố
        city.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));             // Tên tỉnh thành phố
        city.setUnsignedName(FunctionUtils.normalizeAndLowercase(request.getFullName()));   // Tên tỉnh thành phố viết thường không dấu dùng để phục vụ tìm kiếm địa chính

        // Trả về thành công kèm thông tin tỉnh thành phố vừa tạo
        return new SuccessResponse<>(modelMapper.map(cityRepository.save(city), CityDto.class));
    }

    public SuccessResponse<Object> update(UpdateCityRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản đang login có cấp bậc là tỉnh thành phố, quận huyện hoặc phường xã thì không được phép
        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong cities theo id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        City city = cityRepository
                .findByIdAndStatus(request.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(CITY_NOT_EXISTS));

        // Kiểm tra có tỉnh thành phố nào đang hoạt động mà có mã giống với mã truyền xuống hay không ?
        // Nếu có ném ra lỗi
        if (cityRepository.existsByCodeAndStatusAndIdNot(request.getCode(), ACTIVE.name(), city.getId())) {
            throw new ProcessException(CITY_EXISTS_WITH_CODE);
        }

        // Cập nhật thông tin tỉnh thành phố
        city.setCode(request.getCode());                                                    // Mã tỉnh thành phố
        city.setFullName(FunctionUtils.capitalizeFully(request.getFullName()));             // Tên tỉnh thành phố
        city.setUnsignedName(FunctionUtils.normalizeAndLowercase(request.getFullName()));   // Tên tỉnh thành phố viết thường không dấu dùng để phục vụ tìm kiếm địa chính

        // Trả về thành công kèm thông tin tỉnh thành phố vừa cập nhật
        return new SuccessResponse<>(modelMapper.map(cityRepository.save(city), CityDto.class));
    }

    public SuccessResponse<Object> delete(Long id) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản đang login có cấp bậc là tỉnh thành phố, quận huyện hoặc phường xã thì không được phép
        if (loggedAccount.getLevel() > LevelEnums.CENTRAL.value) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong cities theo id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        City city = cityRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(CITY_NOT_EXISTS));

        // Cập nhật trạng thái bản ghi tỉnh thành phố là DELETED
        city.setStatus(DELETED.name());
        cityRepository.save(city);

        // Trả về thành công
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListCityRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Khởi tạo query lấy ra danh sách tỉnh thành phố
        StringBuilder sql = new StringBuilder();
        sql.append(" select id, code, full_name, status from cities where 1 = 1 ");

        // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố muốn tìm kiếm
            sql.append(" and id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());
        }

        // Nếu có dữ liệu mã tỉnh thành phố muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getCode())) {

            // Cộng chuỗi query thêm tìm kiếm theo mã tỉnh thành phố muốn tìm kiếm
            sql.append(" and code = :code ");

            // Set tham số trong query là mã tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("code", request.getCode());
        }

        // Nếu có dữ liệu tên tỉnh thành phố muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getFullName())) {

            // Cộng chuỗi query thêm tìm kiếm theo tên tỉnh thành phố muốn tìm kiếm
            sql.append(" and unsigned_name like concat('%', :name, '%') ");

            // Set tham số trong query là tên tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("name", FunctionUtils.normalizeAndLowercase(request.getFullName()));
        }

        // Nếu không có dữ liệu trạng thái muốn tìm kiếm truyền xuống thì mặc định lấy Hoạt động (ACTIVE)
        String status = request.getStatus();
        if (StringUtils.isBlank(status)) status = ACTIVE.name();

        // Cộng chuỗi query thêm tìm kiếm theo trạng thái bản ghi tỉnh thành phố
        sql.append(" and status = :status ");

        // Set tham số trong query là trạng thái bản ghi tỉnh thành phố muốn tìm kiếm
        sqlParameterSource.addValue("status", status.toUpperCase());

        // Sắp xếp theo mã tỉnh thành phố
        sql.append(" order by code ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng CityDto
        List<CityDto> cities = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(CityDto.class));

        // Trả về thành công kèm danh sách đối tượng CityDto
        return new SuccessResponse<>(cities);
    }
}
