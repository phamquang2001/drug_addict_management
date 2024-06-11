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
import java.util.Base64;
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

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản có thông tin id tỉnh thành phố đơn vị công tác thì chỉ được phép tạo trong tỉnh thành phố của mình
        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
        } else {

            // Lấy ra thông tin id tỉnh thành phố địa chỉ nơi cai nghiện
            cityId = request.getCityId();

            // Kiểm tra xem tỉnh thành phố có tồn tại và hoạt động
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        // Nếu tài khoản có thông tin id quận huyện đơn vị công tác thì chỉ được phép tạo trong quận huyện của mình
        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
        } else {

            // Lấy ra thông tin id quận huyện địa chỉ nơi cai nghiện
            districtId = request.getDistrictId();

            // Kiểm tra xem quận huyện có tồn tại và hoạt động
            if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        // Nếu tài khoản có thông tin id phường xã đơn vị công tác thì chỉ được phép tạo trong phường xã của mình
        Long wardId = null;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            wardId = loggedAccount.getWardId();
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) {

            // Lấy ra thông tin id phường xã địa chỉ nơi cai nghiện
            wardId = request.getWardId();

            // Kiểm tra xem phường xã có tồn tại và hoạt động
            if (!wardRepository.existsByIdAndStatus(wardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
        }

        // Khởi tạo bản ghi nơi cai nghiện mới
        TreatmentPlace treatmentPlace = new TreatmentPlace();
        treatmentPlace.setFullName(request.getFullName());                          // Tên nơi cai nghiện
        treatmentPlace.setLeaderFullName(request.getLeaderFullName());              // Họ tên người đứng đầu
        treatmentPlace.setLeaderIdentifyNumber(request.getLeaderIdentifyNumber());  // Số CCCD người đứng đầu
        treatmentPlace.setLeaderPhoneNumber(request.getLeaderPhoneNumber());        // Số điện thoại người đứng đầu
        treatmentPlace.setLeaderEmail(request.getLeaderEmail());                    // Email người đứng đầu
        treatmentPlace.setStatus(ACTIVE.name());                                    // Trạng thái Hoạt động (ACTIVE)
        treatmentPlace.setCityId(cityId);                                           // ID tỉnh thành phố địa chỉ nơi cai nghiện
        treatmentPlace.setDistrictId(districtId);                                   // ID quận huyện địa chỉ nơi cai nghiện
        treatmentPlace.setWardId(wardId);                                           // ID phường xã địa chỉ nơi cai nghiện
        treatmentPlace.setAddressDetail(request.getAddressDetail());                // Địa chỉ chi tiết nơi cai nghiện

        // Nếu có dữ liệu logo truyền xuống thì lưu vào bản ghi
        if (StringUtils.isNotBlank(request.getLogo())) {
            treatmentPlace.setLogo(Base64.getDecoder().decode(request.getLogo()));
        }

        // Lưu thông tin bản ghi vào bảng treatment_places
        treatmentPlace = treatmentPlaceRepository.save(treatmentPlace);

        // Trả về thành công kèm với thông tin nơi cai nghiện vừa tạo
        return new SuccessResponse<>(convertToTreatmentPlaceDto(treatmentPlace));
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> update(UpdateTreatmentPlacePlaceRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong treatment_places theo id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(request.getId(), StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        // Nếu tài khoản có thông tin id tỉnh thành phố đơn vị công tác
        // => Chỉ được phép cập nhật nơi cai nghiện thuộc tỉnh thành phố đơn vị công tác của mình
        Long cityId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {
            cityId = loggedAccount.getCityId();
            if (!cityId.equals(treatmentPlace.getCityId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {

            // Lấy ra thông tin id tỉnh thành phố địa chỉ nơi cai nghiện muốn cập nhật
            cityId = request.getCityId();

            // Kiểm tra xem tỉnh thành phố có tồn tại và hoạt động
            if (!cityRepository.existsByIdAndStatus(cityId, ACTIVE.name())) {
                throw new BadRequestException(CITY_NOT_EXISTS);
            }
        }

        // Nếu tài khoản có thông tin id quận huyện đơn vị công tác
        // => Chỉ được phép cập nhật nơi cai nghiện thuộc quận huyện đơn vị công tác của mình
        Long districtId;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {
            districtId = loggedAccount.getDistrictId();
            if (!districtId.equals(treatmentPlace.getDistrictId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else {

            // Lấy ra thông tin id quận huyện địa chỉ nơi cai nghiện muốn cập nhật
            districtId = request.getDistrictId();

            // Kiểm tra xem quận huyện có tồn tại và hoạt động
            if (!districtRepository.existsByIdAndStatus(districtId, ACTIVE.name())) {
                throw new BadRequestException(DISTRICT_NOT_EXISTS);
            }
        }

        // Nếu tài khoản có thông tin id phường xã đơn vị công tác
        // => Chỉ được phép cập nhật nơi cai nghiện thuộc phường xã đơn vị công tác của mình
        Long wardId = null;
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {
            wardId = loggedAccount.getWardId();
            if (!wardId.equals(treatmentPlace.getWardId())) {
                throw new ForbiddenException(NOT_ALLOW);
            }
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) { // Nếu có dữ liệu id phường xã địa chỉ nơi cai nghiện muốn cập nhật

            // Lấy ra thông tin id phường xã địa chỉ nơi cai nghiện muốn cập nhật
            wardId = request.getWardId();

            // Kiểm tra xem phường xã có tồn tại và hoạt động
            if (!wardRepository.existsByIdAndStatus(wardId, ACTIVE.name())) {
                throw new BadRequestException(WARD_NOT_EXISTS);
            }
        }

        // Cập nhật thông tin nơi cai nghiện
        treatmentPlace.setFullName(request.getFullName());                          // Tên nơi cai nghiện
        treatmentPlace.setLeaderFullName(request.getLeaderFullName());              // Họ tên người đứng đầu
        treatmentPlace.setLeaderIdentifyNumber(request.getLeaderIdentifyNumber());  // Số CCCD người đứng đầu
        treatmentPlace.setLeaderPhoneNumber(request.getLeaderPhoneNumber());        // Số điện thoại người đứng đầu
        treatmentPlace.setLeaderEmail(request.getLeaderEmail());                    // Email người đứng đầu
        treatmentPlace.setStatus(ACTIVE.name());                                    // Trạng thái Hoạt động (ACTIVE)
        treatmentPlace.setCityId(cityId);                                           // ID tỉnh thành phố địa chỉ nơi cai nghiện
        treatmentPlace.setDistrictId(districtId);                                   // ID quận huyện địa chỉ nơi cai nghiện
        treatmentPlace.setWardId(wardId);                                           // ID phường xã địa chỉ nơi cai nghiện
        treatmentPlace.setAddressDetail(request.getAddressDetail());                // Địa chỉ chi tiết nơi cai nghiện

        // Nếu có dữ liệu logo truyền xuống thì lưu vào bản ghi
        if (StringUtils.isNotBlank(request.getLogo())) {
            treatmentPlace.setLogo(Base64.getDecoder().decode(request.getLogo()));
        }

        // Lưu thông tin bản ghi vào bảng treatment_places
        treatmentPlace = treatmentPlaceRepository.save(treatmentPlace);

        // Trả về thành công kèm với thông tin nơi cai nghiện vừa cập nhật
        return new SuccessResponse<>(convertToTreatmentPlaceDto(treatmentPlace));
    }

    public SuccessResponse<Object> delete(Long id) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm bản ghi trong treatment_places theo id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(id, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        // Nếu tài khoản có id tỉnh thành phố đơn vị công tác
        // => Tài khoản chỉ được phép xóa nơi cai nghiện thuộc tỉnh thành phố đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(treatmentPlace.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản có id quận huyện đơn vị công tác
        // => Tài khoản chỉ được phép xóa nơi cai nghiện thuộc quận huyện đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(treatmentPlace.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản có id phường xã đơn vị công tác
        // => Tài khoản chỉ được phép xóa nơi cai nghiện thuộc phường xã đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(treatmentPlace.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // // Cập nhật trạng thái bản ghi nơi cai nghiện là DELETED
        treatmentPlace.setStatus(DELETED.name());
        treatmentPlaceRepository.save(treatmentPlace);

        // Trả về thành công
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getList(GetListTreatmentPlaceRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Khởi tạo query lấy ra danh sách nơi cai nghiện
        StringBuilder sql = new StringBuilder();
        sql.append(" select *, created_by as txt_created_by, modified_by as txt_modified_by from treatment_places where 1 = 1 ");

        // Nếu tài khoản có id tỉnh thành phố đơn vị công tác
        // => Tài khoản chỉ được phép tìm kiếm nơi cai nghiện thuộc tỉnh thành phố đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố địa chỉ nơi cai nghiện
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố đơn vị công tác của cảnh sát
            sqlParameterSource.addValue("city_id", loggedAccount.getCityId());

        } else if (!FunctionUtils.isNullOrZero(request.getCityId())) { // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố địa chỉ nơi cai nghiện
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu tài khoản có id quận huyện đơn vị công tác
        // => Tài khoản chỉ được phép tìm kiếm nơi cai nghiện thuộc quận huyện đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện địa chỉ nơi cai nghiện
            sql.append(" and district_id = :district_id ");

            // Set tham số trong query là id quận huyện đơn vị công tác của cảnh sát
            sqlParameterSource.addValue("district_id", loggedAccount.getDistrictId());
        } else if (!FunctionUtils.isNullOrZero(request.getDistrictId())) { // Nếu có dữ liệu id quận huyện muốn tìm kiếm

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện địa chỉ nơi cai nghiện
            sql.append(" and district_id = :district_id ");

            // Set tham số trong query là id quận huyện muốn tìm kiếm
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        // Nếu tài khoản có id phường xã đơn vị công tác
        // => Tài khoản chỉ được phép tìm kiếm nơi cai nghiện thuộc phường xã đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id phường xã địa chỉ nơi cai nghiện
            sql.append(" and ward_id = :ward_id ");

            // Set tham số trong query là id phường xã đơn vị công tác của cảnh sát
            sqlParameterSource.addValue("ward_id", loggedAccount.getWardId());
        } else if (!FunctionUtils.isNullOrZero(request.getWardId())) { // Nếu có dữ liệu id phường xã muốn tìm kiếm

            // Cộng chuỗi query thêm tìm kiếm theo id phường xã địa chỉ nơi cai nghiện
            sql.append(" and ward_id = :ward_id ");

            // Set tham số trong query là id phường xã muốn tìm kiếm
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        // Nếu có dữ liệu tên nơi cai nghiện muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getFullName())) {

            // Cộng chuỗi query thêm tìm kiếm theo tên nơi cai nghiện muốn tìm kiếm
            sql.append(" and full_name like concat('%', :full_name, '%') ");

            // Set tham số trong query là tên nơi cai nghiện muốn tìm kiếm
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        // Nếu có dữ liệu họ tên người đứng đầu muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getLeaderFullName())) {

            // Cộng chuỗi query thêm tìm kiếm theo họ tên người đứng đầu muốn tìm kiếm
            sql.append(" and leader_full_name like concat('%', :leader_full_name, '%') ");

            // Set tham số trong query là họ tên người đứng đầu muốn tìm kiếm
            sqlParameterSource.addValue("leader_full_name", request.getLeaderFullName());
        }

        // Nếu có dữ liệu số điện thoại người đứng đầu muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getLeaderPhoneNumber())) {

            // Cộng chuỗi query thêm tìm kiếm theo số điện thoại người đứng đầu muốn tìm kiếm
            sql.append(" and leader_phone_number like concat('%', :leader_phone_number, '%') ");

            // Set tham số trong query là số điện thoại người đứng đầu muốn tìm kiếm
            sqlParameterSource.addValue("leader_phone_number", request.getLeaderPhoneNumber());
        }

        // Nếu không có dữ liệu trạng thái muốn tìm kiếm truyền xuống thì mặc định lấy Hoạt động (ACTIVE)
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Sắp xếp theo ngày tạo từ mới nhất đến cũ nhất
        sql.append(" order by created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng TreatmentPlace
        List<TreatmentPlace> treatmentPlaces = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(TreatmentPlace.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng TreatmentPlaceDto
        List<TreatmentPlaceDto> treatmentPlaceDtos = new ArrayList<>();

        // Duyệt từng phần tử của treatmentPlaces và convert sang TreatmentPlaceDto rồi thêm vào danh sách treatmentPlaceDtos
        treatmentPlaces.forEach(treatmentPlace -> treatmentPlaceDtos.add(convertToTreatmentPlaceDto(treatmentPlace)));

        // Trả về thành công kèm danh sách đối tượng TreatmentPlaceDto
        return new SuccessResponse<>(treatmentPlaceDtos);
    }

    public SuccessResponse<Object> get(Long id) {

        // Tìm kiếm bản ghi trong treatment_places theo id truyền xuống
        // Nếu không tìm thấy hoặc bản ghi đã bị xóa => Ném ra lỗi
        TreatmentPlace treatmentPlace = treatmentPlaceRepository
                .findByIdAndStatus(id, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(TREATMENT_PLACE_NOT_EXISTS));

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản có id tỉnh thành phố đơn vị công tác
        // => Tài khoản chỉ được phép xem nơi cai nghiện thuộc tỉnh thành phố đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getCityId())
                && !loggedAccount.getCityId().equals(treatmentPlace.getCityId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản có id quận huyện đơn vị công tác
        // => Tài khoản chỉ được phép xem nơi cai nghiện thuộc quận huyện đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getDistrictId())
                && !loggedAccount.getDistrictId().equals(treatmentPlace.getDistrictId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Nếu tài khoản có id phường xã đơn vị công tác
        // => Tài khoản chỉ được phép xem nơi cai nghiện thuộc phường xã đơn vị công tác của mình
        if (!FunctionUtils.isNullOrZero(loggedAccount.getWardId())
                && !loggedAccount.getWardId().equals(treatmentPlace.getWardId())) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // // Trả về thành công kèm thông tin chi tiết nơi cai nghiện
        return new SuccessResponse<>(convertToTreatmentPlaceDto(treatmentPlace));
    }
}
