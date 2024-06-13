package com.system.management.service;

import com.system.management.model.dto.*;
import com.system.management.model.entity.*;
import com.system.management.model.request.assign_support.*;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.DrugAddictRepository;
import com.system.management.utils.FunctionUtils;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.AssignStatusEnums.ASSIGNED;
import static com.system.management.utils.enums.AssignStatusEnums.UN_ASSIGN;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;
import static com.system.management.utils.enums.StatusEnums.DELETED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignSupportService extends BaseCommonService {

    private final DrugAddictRepository drugAddictRepository;

    public SuccessResponse<Object> isAssigned(Long drugAddictId) {

        // Tìm kiếm thông tin đối tượng nghiện hút trong bảng drug_addicts
        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(drugAddictId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

        // Tìm kiếm bản ghi trong assign_supports để kiểm tra xem đối tượng đã được phân công cho cảnh sát nào giám sát hay chưa ?
        AssignSupport assignSupport = assignSupportRepository
                .findByDrugAddictIdAndStatus(drugAddict.getId(), ACTIVE.name()).orElse(null);

        // Nếu không tìm thấy => Chưa được phân công cho cảnh sát nào => Trả về thành công
        if (assignSupport == null) {
            return new SuccessResponse<>();
        }

        // Ngược lại nếu tìm thấy bản ghi trong assign_supports

        // Tìm kiếm thông tin cảnh sát đã được phân công giám sát đối tượng trong bảng polices
        Police police = policeRepository
                .findByIdAndStatus(assignSupport.getPoliceId(), StatusEnums.ACTIVE.name()).orElse(null);

        // Nếu không tìm thấy => Cảnh sát đã bị xóa
        if (police == null) {

            // Cập nhật lại trạng thái bản ghi trong assign_supports về DELETED để có thể gán đối tượng này cho cảnh sát khác
            assignSupport.setStatus(DELETED.name());
            assignSupportRepository.save(assignSupport);

            // Trả về thành công
            return new SuccessResponse<>();
        }

        // Ngược lại nếu tìm thấy thông tin cảnh sát trong bảng polices
        // => Gen ra thông báo lỗi hiển thị lên FE
        String error = IS_ASSIGNED.replace("$[0]", drugAddict.getFullName())
                .replace("$[1]", drugAddict.getIdentifyNumber())
                .replace("$[2]", police.getFullName())
                .replace("$[3]", police.getIdentifyNumber());

        // Ném ra lỗi + message mong muốn hiển thị cho FE
        throw new BadRequestException(error);
    }

    public SuccessResponse<Object> assignDrugAddict(AssignDrugAddictRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép phân công giám sát đối tượng cho cảnh sát khác
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Lấy ra thông tin phân công
        Long policeId = request.getPoliceId();          // ID cảnh sát được phân công
        Long drugAddictId = request.getDrugAddictId();  // ID đối tượng

        // Nếu đã tồn tại bản ghi trong assign_supports theo id cảnh sát và đối tượng truyền xuống
        // => Cảnh sát được phân công giám sát đối tượng mong muốn => Ném ra lỗi
        if (assignSupportRepository.existsByPoliceIdAndDrugAddictIdAndStatus(policeId, drugAddictId, ACTIVE.name())) {
            throw new BadRequestException(ALREADY_ASSIGNED_DRUG_ADDICT);
        }

        // Tìm kiếm thông tin cảnh sát trong bảng polices => Nếu không tìm thấy thì ném ra lỗi
        Police police = policeRepository
                .findByIdAndStatus(policeId, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        // Tìm kiếm thông tin đối tượng trong bảng drug_addicts => Nếu không tìm thấy thì ném ra lỗi
        DrugAddict drugAddict = drugAddictRepository
                .findByIdAndStatus(drugAddictId, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(DRUG_ADDICT_NOT_EXISTS));

        // Nếu đối tượng đã được phân công giám sát cho cảnh sát khác
        if (!FunctionUtils.isNullOrZero(drugAddict.getPoliceId())) {

            // Tìm kiếm bản ghi trong assign_supports theo ID đối tượng và ID cảnh sát đang được phân công giám sát
            AssignSupport assignSupport = assignSupportRepository
                    .findByDrugAddictIdAndPoliceId(drugAddict.getId(), drugAddict.getPoliceId()).orElse(null);

            // Nếu tìm thấy
            if (assignSupport != null) {
                // Cập nhật lại trạng thái bản ghi trong assign_supports về DELETED để có thể gán đối tượng này cho cảnh sát khác
                assignSupport.setStatus(DELETED.name());
                assignSupportRepository.save(assignSupport);
            }
        }

        // Cập nhật lại giá trị ID cảnh sát giám sát đối tượng
        drugAddict.setPoliceId(police.getId());
        drugAddictRepository.save(drugAddict);

        // Tạo bản ghi mới trong assign_supports
        AssignSupport assignSupport = new AssignSupport();
        assignSupport.setPoliceId(police.getId());          // ID cảnh sát giám sát đối tượng
        assignSupport.setDrugAddictId(drugAddict.getId());  // ID đối tượng
        assignSupport.setStatus(ACTIVE.name());             // Trạng thái ACTIVE
        assignSupportRepository.save(assignSupport);        // Lưu vào database

        // Set lại trạng thái phân công của cảnh sát về Đã phân công
        police.setAssignStatus(ASSIGNED.getValue());
        policeRepository.save(police);

        // Trả về thành công kèm dữ liệu bản ghi assign_supports vừa tạo
        return new SuccessResponse<>(convertToAssignSupportDto(assignSupport));
    }

    public SuccessResponse<Object> assignCadastral(AssignCadastralRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép phân công hỗ trợ địa chính cho cảnh sát khác
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Lấy ra thông tin phân công
        Long policeId = request.getPoliceId();      // ID cảnh sát được phân công
        Integer levelValue = request.getLevel();    // Cấp địa chính được phân công
        Long cityId = request.getCityId();          // ID tỉnh thành phố được phân công
        Long districtId = request.getDistrictId();  // ID quận huyện được phân công
        Long wardId = request.getWardId();          // ID phường xã được phân công

        // Nếu đã tồn tại bản ghi trong assign_supports theo id cảnh sát và id địa chính truyền xuống
        // => Cảnh sát được phân công hỗ trợ địa chính mong muốn => Ném ra lỗi
        if (assignSupportRepository.existsByPoliceIdAndCityIdAndDistrictIdAndWardIdAndStatus(
                policeId, cityId, districtId, wardId, ACTIVE.name())) {
            throw new BadRequestException(ALREADY_ASSIGNED_CADASTRAL);
        }

        // Tìm kiếm thông tin cảnh sát trong bảng polices => Nếu không tìm thấy thì ném ra lỗi
        Police police = policeRepository
                .findByIdAndStatus(policeId, StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(POLICE_NOT_EXISTS));

        // Nếu id địa chính được phân công trùng với địa chính đơn vị đang công tác của cảnh sát => Ném ra lỗi
        if (Objects.equals(cityId, police.getCityId())
                && Objects.equals(districtId, police.getCityId())
                && Objects.equals(wardId, police.getWardId())) {
            throw new BadRequestException(NOT_ALLOW_ASSIGNED_CADASTRAL);
        }

        // Kiểm tra dữ liệu cấp địa chính truyền xuống có trong danh mục quy định
        LevelEnums level = LevelEnums.dict.get(levelValue);
        if (level == null) {
            throw new BadRequestException(INVALID_LEVEL);
        }

        // Nếu cấp địa chính được phân công là từ cấp tỉnh thành phố trở xuống (level > 1)
        // => Bắt buộc phải có id tỉnh thành phố truyền xuống và phải có dữ liệu tỉnh thành phố trong bảng cities theo id đấy
        if (levelValue > LevelEnums.CENTRAL.value
                && (FunctionUtils.isNullOrZero(cityId) || !cityRepository.existsByIdAndStatus(cityId, ACTIVE.name()))) {
            throw new BadRequestException(CITY_NOT_EXISTS);
        }

        // Nếu cấp địa chính được phân công là từ cấp quận huyện trở xuống (level > 2)
        // => Bắt buộc phải có id quận huyện truyền xuống và phải có dữ liệu quận huyện trong bảng districts theo id đấy
        if (levelValue > LevelEnums.CITY.value
                && (FunctionUtils.isNullOrZero(districtId) || !districtRepository.existsByIdAndStatus(districtId, ACTIVE.name()))) {
            throw new BadRequestException(DISTRICT_NOT_EXISTS);
        }

        // Nếu cấp địa chính được phân công là cấp phường xã (level > 3)
        // => Bắt buộc phải có id phường xã truyền xuống và phải có dữ liệu phường xã trong bảng wards theo id đấy
        if (levelValue > LevelEnums.DISTRICT.value
                && (FunctionUtils.isNullOrZero(wardId) || !wardRepository.existsByIdAndStatus(wardId, ACTIVE.name()))) {
            throw new BadRequestException(WARD_NOT_EXISTS);
        }

        // Tạo bản ghi mới trong assign_supports
        AssignSupport assignSupport = new AssignSupport();
        assignSupport.setPoliceId(police.getId());      // ID cảnh sát được phân công
        assignSupport.setCityId(cityId);                // ID tỉnh thành phố hỗ trợ
        assignSupport.setDistrictId(districtId);        // ID quận huyện hỗ trợ
        assignSupport.setWardId(wardId);                // ID phường xã hỗ trợ
        assignSupport.setLevel(level.value);            // Cấp địa chính hỗ trợ
        assignSupport.setStatus(ACTIVE.name());         // Trạng thái ACTIVE
        assignSupportRepository.save(assignSupport);    // Lưu vào database

        // Set lại trạng thái phân công của cảnh sát về Đã phân công
        police.setAssignStatus(ASSIGNED.getValue());
        policeRepository.save(police);

        // Trả về thành công kèm dữ liệu bản ghi assign_supports vừa tạo
        return new SuccessResponse<>(convertToAssignSupportDto(assignSupport));
    }

    public SuccessResponse<Object> delete(Long id) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép xóa phân công giám sát hay hỗ trợ của cảnh sát khác
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Tìm kiếm thông tin bản ghi trong assign_supports theo id truyền xuống
        // Nếu không tìm thấy tức là bản ghi không tồn tại hoặc đã bị xóa => Ném ra lỗi
        AssignSupport assignSupport = assignSupportRepository
                .findByIdAndStatus(id, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ASSIGN_SUPPORT_NOT_EXISTS));

        // Cập nhật trạng thái bản ghi assign_supports về DELETED
        assignSupport.setStatus(DELETED.name());
        assignSupportRepository.save(assignSupport);

        // Nếu cảnh sát gắn với bản ghi assign_supports không còn tổn tại bản ghi nào đang hoạt động trong assign_supports
        if (!assignSupportRepository.existsByPoliceIdAndStatus(assignSupport.getPoliceId(), ACTIVE.name())) {

            // Tìm kiếm thông tin cảnh sát trong bảng polices
            Police police = policeRepository.findById(assignSupport.getPoliceId()).orElse(null);

            // Nếu tìm thấy thì cập nhật trạng thái phân công của cảnh sát là Chưa phân công
            if (police != null) {
                police.setAssignStatus(UN_ASSIGN.getValue());
                policeRepository.save(police);
            }
        }

        // Trả về thành công
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getListAssignedDrugAddict(GetListAssignedDrugAddictRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép lấy ra danh sách đối tượng đã được phân công của cảnh sát
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Khởi tạo query lấy ra dữ liệu đối tượng đã được phân công của cảnh sát
        StringBuilder sql = new StringBuilder();
        sql.append(" select a.id                       as id,");
        sql.append("        b.identify_number          as da_identify_number,");
        sql.append("        b.full_name                as da_full_name,");
        sql.append("        b.permanent_ward_id        as da_permanent_ward_id,");
        sql.append("        b.permanent_district_id    as da_permanent_district_id,");
        sql.append("        b.permanent_city_id        as da_permanent_city_id,");
        sql.append("        b.permanent_address_detail as da_permanent_address_detail,");
        sql.append("        a.police_id                as police_id,");
        sql.append("        a.drug_addict_id           as drug_addict_id,");
        sql.append("        a.created_at               as created_at,");
        sql.append("        a.created_by               as txt_created_by");
        sql.append(" from assign_supports a join drug_addicts b on a.drug_addict_id = b.id");
        sql.append(" where a.police_id = :police_id and a.drug_addict_id is not null");

        // Set tham số trong query là id cảnh sát muốn tìm kiếm
        sqlParameterSource.addValue("police_id", request.getPoliceId());

        // Nếu có dữ liệu số CCCD của đối tượng muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {

            // Cộng chuỗi query thêm tìm kiếm theo số CCCD của đối tượng
            sql.append(" and b.identify_number like concat('%', :identify_number, '%') ");

            // Set tham số trong query là số CCCD của đối tượng
            sqlParameterSource.addValue("identify_number", request.getIdentifyNumber());
        }

        // Nếu có dữ liệu họ tên của đối tượng muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getFullName())) {

            // Cộng chuỗi query thêm tìm kiếm theo họ tên của đối tượng
            sql.append(" and b.full_name like concat('%', :full_name, '%') ");

            // Set tham số trong query là họ tên của đối tượng
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        // Nếu có dữ liệu id tỉnh thành phố nơi ở thường trú của đối tượng muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố nơi ở thường trú của đối tượng
            sql.append(" and b.permanent_city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố nơi ở thường trú của đối tượng
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu có dữ liệu id quận huyện nơi ở thường trú của đối tượng muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện nơi ở thường trú của đối tượng
            sql.append(" and b.permanent_district_id = :district_id ");

            // Set tham số trong query là id quận huyện nơi ở thường trú của đối tượng
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        // Nếu có dữ liệu id phường xã nơi ở thường trú của đối tượng muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getWardId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id phường xã nơi ở thường trú của đối tượng
            sql.append(" and b.permanent_ward_id = :ward_id ");

            // Set tham số trong query là id phường xã nơi ở thường trú của đối tượng
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        // Nếu có dữ liệu ngày bắt đầu khoảng thời gian tìm kiếm được phân công
        if (request.getStartDate() != null) {

            // Cộng chuỗi query thêm tìm kiếm theo ngày bắt đầu khoảng thời gian tìm kiếm được phân công
            sql.append(" and DATE (a.created_at) >= DATE (:start_date) ");

            // Set tham số trong query là ngày bắt đầu khoảng thời gian tìm kiếm được phân công
            sqlParameterSource.addValue("start_date", request.getStartDate());
        }

        // Nếu có dữ liệu ngày kết thúc khoảng thời gian tìm kiếm được phân công
        if (request.getEndDate() != null) {

            // Cộng chuỗi query thêm tìm kiếm theo ngày kết thúc khoảng thời gian tìm kiếm được phân công
            sql.append(" and DATE (a.created_at) <= DATE (:end_date) ");

            // Set tham số trong query là ngày kết thúc khoảng thời gian tìm kiếm được phân công
            sqlParameterSource.addValue("end_date", request.getEndDate());
        }

        // Set trạng thái bản ghi assign_supports của cảnh sát bắt buộc phải là ACTIVE thì mới tính
        sql.append(" and a.status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Sắp xếp theo ngày tạo bản ghi assign_supports là từ mới nhất đến cũ nhất
        sql.append(" order by a.created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng AssignSupport
        List<AssignSupport> assignSupports = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(AssignSupport.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng AssignSupportDto
        List<AssignSupportDto> assignSupportDtos = new ArrayList<>();

        // Duyệt từng phần tử của assignSupports và convert sang AssignSupportDto rồi thêm vào danh sách assignSupportDtos
        assignSupports.forEach(item -> assignSupportDtos.add(convertToAssignSupportDto(item)));

        // Trả về thành công kèm danh sách đối tượng AssignSupportDto
        return new SuccessResponse<>(assignSupportDtos);
    }

    public SuccessResponse<Object> getListUnassignedDrugAddict(GetListUnassignedDrugAddictRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép lấy ra danh sách đối tượng chưa được phân công của cảnh sát
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Khởi tạo query lấy ra dữ liệu đối tượng chưa được phân công của cảnh sát
        StringBuilder sql = new StringBuilder();
        sql.append(" select id                       as id,");
        sql.append("        identify_number          as identify_number,");
        sql.append("        full_name                as full_name,");
        sql.append("        permanent_ward_id        as permanent_ward_id,");
        sql.append("        permanent_district_id    as permanent_district_id,");
        sql.append("        permanent_city_id        as permanent_city_id,");
        sql.append("        permanent_address_detail as permanent_address_detail");
        sql.append(" from drug_addicts where (police_id is null or police_id <> :police_id)");

        // Set tham số trong query là id cảnh sát muốn tìm kiếm
        sqlParameterSource.addValue("police_id", request.getPoliceId());

        // Nếu có dữ liệu số CCCD của đối tượng muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getIdentifyNumber())) {

            // Cộng chuỗi query thêm tìm kiếm theo số CCCD của đối tượng
            sql.append(" and identify_number like concat('%', :identify_number, '%') ");

            // Set tham số trong query là số CCCD của đối tượng
            sqlParameterSource.addValue("identify_number", request.getIdentifyNumber());
        }

        // Nếu có dữ liệu họ tên của đối tượng muốn tìm kiếm
        if (StringUtils.isNotBlank(request.getFullName())) {

            // Cộng chuỗi query thêm tìm kiếm theo họ tên của đối tượng
            sql.append(" and full_name like concat('%', :full_name, '%') ");

            // Set tham số trong query là họ tên của đối tượng
            sqlParameterSource.addValue("full_name", request.getFullName());
        }

        // Nếu có dữ liệu id tỉnh thành phố nơi ở thường trú của đối tượng muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố nơi ở thường trú của đối tượng
            sql.append(" and permanent_city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố nơi ở thường trú của đối tượng
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu có dữ liệu id quận huyện nơi ở thường trú của đối tượng muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện nơi ở thường trú của đối tượng
            sql.append(" and permanent_district_id = :district_id ");

            // Set tham số trong query là id quận huyện nơi ở thường trú của đối tượng
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        // Nếu có dữ liệu id phường xã nơi ở thường trú của đối tượng muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getWardId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id phường xã nơi ở thường trú của đối tượng
            sql.append(" and permanent_ward_id = :ward_id ");

            // Set tham số trong query là id phường xã nơi ở thường trú của đối tượng
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        // Set trạng thái của đối tượng bắt buộc phải là ACTIVE thì mới tính
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Sắp xếp theo ngày tạo bản ghi đối tượng là từ mới nhất đến cũ nhất
        sql.append(" order by created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng DrugAddict
        List<DrugAddict> drugAddicts = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(DrugAddict.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng DrugAddictDto
        List<DrugAddictDto> drugAddictDtos = new ArrayList<>();

        // Duyệt từng phần tử của drugAddicts và convert sang DrugAddictDto rồi thêm vào danh sách drugAddictDtos
        drugAddicts.forEach(item -> drugAddictDtos.add(convertToDrugAddictDto(item)));

        // Trả về thành công kèm danh sách đối tượng DrugAddictDto
        return new SuccessResponse<>(drugAddictDtos);
    }

    public SuccessResponse<Object> getListAssignedCadastral(GetListAssignCadastralRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép lấy ra danh sách địa chính đã được phân công của cảnh sát
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Khởi tạo query lấy ra dữ liệu địa chính đã được phân công của cảnh sát
        StringBuilder sql = new StringBuilder();
        sql.append(" select id, city_id, district_id, ward_id, level, created_at, created_by as txt_created_by from assign_supports ");
        sql.append(" where police_id = :police_id and city_id is not null ");

        // Set tham số trong query là id cảnh sát muốn tìm kiếm
        sqlParameterSource.addValue("police_id", request.getPoliceId());

        // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu có dữ liệu id quận huyện muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện
            sql.append(" and district_id = :district_id ");

            // Set tham số trong query là id quận huyện
            sqlParameterSource.addValue("district_id", request.getDistrictId());
        }

        // Nếu có dữ liệu id phường xã muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getWardId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id phường xã
            sql.append(" and ward_id = :ward_id ");

            // Set tham số trong query là id phường xã
            sqlParameterSource.addValue("ward_id", request.getWardId());
        }

        // Nếu có dữ liệu ngày bắt đầu khoảng thời gian tìm kiếm được phân công
        if (request.getStartDate() != null) {

            // Cộng chuỗi query thêm tìm kiếm theo ngày bắt đầu khoảng thời gian tìm kiếm được phân công
            sql.append(" and DATE (created_at) >= DATE (:start_date) ");

            // Set tham số trong query là ngày bắt đầu khoảng thời gian tìm kiếm được phân công
            sqlParameterSource.addValue("start_date", request.getStartDate());
        }

        // Nếu có dữ liệu ngày kết thúc khoảng thời gian tìm kiếm được phân công
        if (request.getEndDate() != null) {

            // Cộng chuỗi query thêm tìm kiếm theo ngày kết thúc khoảng thời gian tìm kiếm được phân công
            sql.append(" and DATE (created_at) <= DATE (:end_date) ");

            // Set tham số trong query là ngày kết thúc khoảng thời gian tìm kiếm được phân công
            sqlParameterSource.addValue("end_date", request.getEndDate());
        }

        // Set trạng thái bản ghi assign_supports của cảnh sát bắt buộc phải là ACTIVE thì mới tính
        sql.append(" and status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Sắp xếp theo ngày tạo bản ghi assign_supports là từ mới nhất đến cũ nhất
        sql.append(" order by created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng AssignSupport
        List<AssignSupport> assignSupports = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(AssignSupport.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng AssignSupportDto
        List<AssignSupportDto> assignSupportDtos = new ArrayList<>();

        // Duyệt từng phần tử của assignSupports và convert sang AssignSupportDto rồi thêm vào danh sách assignSupportDtos
        assignSupports.forEach(item -> assignSupportDtos.add(convertToAssignSupportDto(item)));

        // Trả về thành công kèm danh sách đối tượng AssignSupportDto
        return new SuccessResponse<>(assignSupportDtos);
    }

    public SuccessResponse<Object> getListUnassignedCadastral(GetListUnassignedCadastralRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Nếu tài khoản đang login không phải Cảnh sát trưởng thì không được phép lấy ra danh sách địa chính chưa được phân công của cảnh sát
        if (!Objects.equals(loggedAccount.getRole(), RoleEnums.SHERIFF.value)) {
            throw new ForbiddenException(NOT_ALLOW);
        }

        // Dựa theo cấp bậc địa chính tìm kiếm mà điều hướng xử lý
        if (Objects.equals(request.getLevel(), LevelEnums.CITY.value)) {            // Cấp tỉnh thành phố
            return getListUnassignedCity(request);
        } else if (Objects.equals(request.getLevel(), LevelEnums.DISTRICT.value)) { // Cấp quận huyện
            return getListUnassignedDistrict(request);
        } else if (Objects.equals(request.getLevel(), LevelEnums.WARD.value)) {     // Cấp phường xã
            return getListUnassignedWard(request);
        }

        // Trả về thành công
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> getListUnassignedCity(GetListUnassignedCadastralRequest request) {

        // Khởi tạo query lấy ra dữ liệu danh sách id tỉnh thành phố đã được phân công cho cảnh sát
        StringBuilder sql = new StringBuilder();
        sql.append(" select city_id from assign_supports where police_id = :police_id and level = :level ");

        // Set tham số trong query là id cảnh sát muốn tìm kiếm và cấp địa chính là tỉnh thành phố
        sqlParameterSource.addValue("police_id", request.getPoliceId());
        sqlParameterSource.addValue("level", LevelEnums.CITY.value);

        // Thực thi query lấy ra danh sách id tỉnh thành phố đã được phân công cho cảnh sát
        List<Long> ids = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, SingleColumnRowMapper.newInstance(Long.class));

        // Khởi tạo query lấy ra dữ liệu thông tin tỉnh thành phố đang hoạt động
        sql = new StringBuilder();
        sql.append(" select id, code, full_name from cities where status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Loại bỏ các tỉnh thành phố cảnh sát đã được phân công
        if (!ids.isEmpty()) {
            sql.append(" and id not in (:ids) ");
            sqlParameterSource.addValue("ids", ids);
        }

        // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố muốn tìm kiếm
            sql.append(" and id = :id ");

            // Set tham số trong query là id tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("id", request.getCityId());
        }

        // Sắp xếp theo ngày tạo bản ghi cities là từ mới nhất đến cũ nhất
        sql.append(" order by created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng City
        List<City> cities = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(City.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng CityDto
        List<CityDto> cityDtos = new ArrayList<>();

        // Duyệt từng phần tử của cities và convert sang City rồi thêm vào danh sách cityDtos
        cities.forEach(city -> cityDtos.add(modelMapper.map(city, CityDto.class)));

        // Trả về thành công kèm danh sách đối tượng CityDto
        return new SuccessResponse<>(cityDtos);
    }

    public SuccessResponse<Object> getListUnassignedDistrict(GetListUnassignedCadastralRequest request) {

        // Khởi tạo query lấy ra dữ liệu danh sách id quận huyện đã được phân công cho cảnh sát
        StringBuilder sql = new StringBuilder();
        sql.append(" select district_id from assign_supports where police_id = :police_id and level = :level ");

        // Set tham số trong query là id cảnh sát muốn tìm kiếm và cấp địa chính là quận huyện
        sqlParameterSource.addValue("police_id", request.getPoliceId());
        sqlParameterSource.addValue("level", LevelEnums.DISTRICT.value);

        // Thực thi query lấy ra danh sách id quận huyện đã được phân công cho cảnh sát
        List<Long> ids = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, SingleColumnRowMapper.newInstance(Long.class));

        // Khởi tạo query lấy ra dữ liệu thông tin quận huyện đang hoạt động
        sql = new StringBuilder();
        sql.append(" select id, code, full_name, city_id from districts where status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Loại bỏ các quận huyện cảnh sát đã được phân công
        if (!ids.isEmpty()) {
            sql.append(" and id not in (:ids) ");
            sqlParameterSource.addValue("ids", ids);
        }

        // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố muốn tìm kiếm
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu có dữ liệu id quận huyện muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện muốn tìm kiếm
            sql.append(" and id = :id ");

            // Set tham số trong query là id quận huyện muốn tìm kiếm
            sqlParameterSource.addValue("id", request.getDistrictId());
        }

        // Sắp xếp theo ngày tạo bản ghi districts là từ mới nhất đến cũ nhất
        sql.append(" order by created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng District
        List<District> districts = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(District.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng DistrictDto
        List<DistrictDto> districtDtos = new ArrayList<>();

        // Duyệt từng phần tử của districts và convert sang DistrictDto rồi thêm vào danh sách districtDtos
        districts.forEach(district -> {
            DistrictDto districtDto = modelMapper.map(district, DistrictDto.class);
            districtDto.setCity(findCityByIdWithoutAuditor(districtDto.getCityId()));
            districtDtos.add(districtDto);

        });

        // Trả về thành công kèm danh sách đối tượng DistrictDto
        return new SuccessResponse<>(districtDtos);
    }

    public SuccessResponse<Object> getListUnassignedWard(GetListUnassignedCadastralRequest request) {

        // Khởi tạo query lấy ra dữ liệu danh sách id phường xã đã được phân công cho cảnh sát
        StringBuilder sql = new StringBuilder();
        sql.append(" select ward_id from assign_supports where police_id = :police_id and level = :level ");

        // Set tham số trong query là id cảnh sát muốn tìm kiếm và cấp địa chính là phường xã
        sqlParameterSource.addValue("police_id", request.getPoliceId());
        sqlParameterSource.addValue("level", LevelEnums.WARD.value);

        // Thực thi query lấy ra danh sách id phường xã đã được phân công cho cảnh sát
        List<Integer> ids = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, new SingleColumnRowMapper<>(Integer.class));

        // Khởi tạo query lấy ra dữ liệu thông tin phường xã đang hoạt động
        sql = new StringBuilder();
        sql.append(" select id, code, full_name, city_id, district_id from wards where status = :status ");
        sqlParameterSource.addValue("status", ACTIVE.name());

        // Loại bỏ các phường xã cảnh sát đã được phân công
        if (!ids.isEmpty()) {
            sql.append(" and id not in (:ids) ");
            sqlParameterSource.addValue("ids", ids);
        }

        // Nếu có dữ liệu id tỉnh thành phố muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getCityId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id tỉnh thành phố muốn tìm kiếm
            sql.append(" and city_id = :city_id ");

            // Set tham số trong query là id tỉnh thành phố muốn tìm kiếm
            sqlParameterSource.addValue("city_id", request.getCityId());
        }

        // Nếu có dữ liệu id quận huyện muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getDistrictId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id quận huyện muốn tìm kiếm
            sql.append(" and id = :id ");

            // Set tham số trong query là id quận huyện muốn tìm kiếm
            sqlParameterSource.addValue("id", request.getDistrictId());
        }

        // Nếu có dữ liệu id phường xã muốn tìm kiếm
        if (!FunctionUtils.isNullOrZero(request.getWardId())) {

            // Cộng chuỗi query thêm tìm kiếm theo id phường xã muốn tìm kiếm
            sql.append(" and id = :id ");

            // Set tham số trong query là id phường xã muốn tìm kiếm
            sqlParameterSource.addValue("id", request.getWardId());
        }

        // Sắp xếp theo ngày tạo bản ghi wards là từ mới nhất đến cũ nhất
        sql.append(" order by created_at desc ");

        // Lấy ra thông tin phân trang truyền xuống
        // Nếu không có thì mặc định lấy ra trang đầu tiên (page = 1) và số lượng bản ghi trên trang là 100 (size = 100)
        int page = FunctionUtils.isNullOrZero(request.getPage()) ? 1 : request.getPage();
        int size = FunctionUtils.isNullOrZero(request.getSize()) ? 100 : request.getSize();

        // Cộng chuỗi query thông tin phân trang
        sql.append(" limit :page, :size ");
        sqlParameterSource.addValue("page", (page - 1) * size); // Số thứ tự trang
        sqlParameterSource.addValue("size", size);                    // Số lượng bản ghi trên trang

        // Thực thi query và trả về danh sách kết quả là một list đối tượng Ward
        List<Ward> wards = namedParameterJdbcTemplate
                .query(sql.toString(), sqlParameterSource, BeanPropertyRowMapper.newInstance(Ward.class));

        // Khởi tạo danh sách kết quả trả ra cho FE là một list danh sách đối tượng WardDto
        List<WardDto> wardDtos = new ArrayList<>();

        // Duyệt từng phần tử của wards và convert sang WardDto rồi thêm vào danh sách wardDtos
        wards.forEach(ward -> {
            WardDto wardDto = modelMapper.map(ward, WardDto.class);
            wardDto.setCity(findCityByIdWithoutAuditor(wardDto.getCityId()));
            wardDto.setDistrict(findDistrictByIdWithoutAuditor(wardDto.getDistrictId()));
            wardDtos.add(wardDto);
        });

        // Trả về thành công kèm danh sách đối tượng WardDto
        return new SuccessResponse<>(wardDtos);
    }
}
