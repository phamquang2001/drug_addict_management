package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.BlackList;
import com.system.management.model.entity.Police;
import com.system.management.model.entity.PoliceRequest;
import com.system.management.model.entity.RefreshToken;
import com.system.management.model.request.auth.ChangePasswordRequest;
import com.system.management.model.request.auth.LoginRequest;
import com.system.management.model.request.auth.UpdateAccountRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.BlackListRepository;
import com.system.management.repository.PoliceRequestRepository;
import com.system.management.repository.RefreshTokenRepository;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.enums.GenderEnums;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.enums.StatusEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ProcessException;
import com.system.management.utils.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.system.management.utils.constants.ErrorMessage.*;
import static com.system.management.utils.enums.StatusEnums.ACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends BaseCommonService {

    public static final String USERNAME = "username";
    public static final String ROLE = "role";
    public static final String ROLE_NAME = "roleName";
    public static final String LEVEL = "level";
    public static final String LEVEL_NAME = "levelName";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ACCESS_TOKEN_EXPIRY = "accessTokenExpiry";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String REFRESH_TOKEN_EXPIRY = "refreshTokenExpiry";

    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final BlackListRepository blackListRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PoliceRequestRepository policeRequestRepository;

    @Value("${config.jwt_key}")
    private String jwtKey;
    @Value("${config.expire_time.access_token}")
    private Long expireTimeAccessToken;
    @Value("${config.expire_time.refresh_token}")
    private Long expireTimeRefreshToken;

    public SuccessResponse<Object> login(LoginRequest request) {
        // Tìm kiếm thông tin cảnh sát theo tên đăng nhập (số CCCD) => Nếu không tìm thấy thì ném ra lỗi
        Police police = policeRepository
                .findByIdentifyNumberAndStatus(request.getUsername(), StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        // Kiểm tra mật khẩu truyền xuống có khớp với mật khẩu đang lưu trong bảng polices
        // Bằng cách băm mật khẩu truyền xuống và so mã băm đó với mã băm trong bảng polices
        // Nếu 2 mã băm khác nhau thì ném ra lỗi
        if (!passwordEncoder.matches(request.getPassword(), police.getPassword())) {
            throw new ProcessException(WRONG_PASSWORD);
        }

        // Sinh access-token cho tài khoản
        Map<?, ?> accessToken = generateAccessToken(police);

        // Sinh refresh-token cho tài khoản
        Map<?, ?> refreshToken = generateRefreshToken(police);

        // Trả về thông tin đăng nhập
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(USERNAME, request.getUsername());                              // Tên tài khoản (Số CCCD)
        data.put(ROLE, police.getRole());                                       // Dữ liệu vai trò của tài khoản
        data.put(ROLE_NAME, RoleEnums.dict.get(police.getRole()).label);        // Tai vai trò của tài khoản
        data.put(LEVEL, police.getLevel());                                     // Dữ liệu cấp bậc tài khoản
        data.put(LEVEL_NAME, LevelEnums.dict.get(police.getLevel()).label);     // Tên cấp bậc tài khoản
        data.put(ACCESS_TOKEN, accessToken.get(ACCESS_TOKEN));                  // Dữ liệu access-token
        data.put(ACCESS_TOKEN_EXPIRY, accessToken.get(ACCESS_TOKEN_EXPIRY));    // Access-token có hiệu lực trong bao lâu ?
        data.put(REFRESH_TOKEN, refreshToken.get(REFRESH_TOKEN));               // Dữ liệu refresh-token
        data.put(REFRESH_TOKEN_EXPIRY, refreshToken.get(REFRESH_TOKEN_EXPIRY)); // Refresh-token có hiệu lực trong bao lâu ?

        // Trả về thành công kèm thông tin đăng nhập
        return new SuccessResponse<>(data);
    }

    public SuccessResponse<Object> verify(String token) {
        // Nếu không có access-token truyền xuống thì ném ra lỗi
        if (StringUtils.isBlank(token)) {
            throw new BadRequestException(INVALID_ACCESS_TOKEN);
        }

        // Loại bỏ chữ Bearer ở đầu token
        token = token.replace("Bearer", "").trim();

        // Nếu token đang nằm trong black list (Tài khoản gắn với access-token đã đăng xuất) thì không được phép sử dụng lại token này nữa
        if (blackListRepository.existsByToken(token)) {
            throw new ProcessException(BLOCKED_ACCESS_TOKEN);
        }

        // Giải mã access-token để lấy ra thông tin dăng nhập
        Jws<Claims> claimsJws;
        try {
            // Sử dụng thư viện io.jsonwebtoken.* để giải mã
            claimsJws = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(token);
        } catch (ExpiredJwtException ex) { // Trường hợp giải mã báo lỗi token đã hết hạn
            throw new UnauthorizedException(504, EXPIRED_ACCESS_TOKEN);
        } catch (Exception ex) { // Trường hợp giải mã báo lỗi khác ngoài token đã hết hạn
            throw new UnauthorizedException(VERIFY_TOKEN_FAILED);
        }

        // Tìm kiếm thông tin cảnh sát đang đăng nhập trong bảng polices theo số CCCD được lưu ở access-token
        // Nếu không tìm thấy hoặc thông tin cảnh sát đã bị xóa => Ném ra lỗi
        Police police = policeRepository
                .findByIdentifyNumberAndStatus(claimsJws.getBody().getSubject(), StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        // Trả về thành công kèm thông tin cảnh sát đang đăng nhập
        return new SuccessResponse<>(convertToPoliceDto(police));
    }

    public SuccessResponse<Object> logout(String token) {
        // Nếu không có access-token truyền xuống thì ném ra lỗi
        if (StringUtils.isBlank(token)) {
            throw new BadRequestException(INVALID_ACCESS_TOKEN);
        }

        // Tạo bản ghi mới trong black-list
        BlackList blackList = new BlackList();
        blackList.setToken(token.replace("Bearer", "").trim()); // Access-token tài khoản đang sử dụng
        blackList.setBlockDate(new Date());                                      // Thời điểm bị chặn (Thời điểm tài khoản đăng xuất)
        blackListRepository.save(blackList);

        // Trả về thành công
        return new SuccessResponse<>();
    }

    private Map<String, Object> generateAccessToken(Police police) {
        // Lấy ra thời điểm hiện tại
        Date now = new Date();

        // Sinh access-token
        String accessToken = Jwts.builder()
                .setSubject(police.getIdentifyNumber())
                .claim(ROLE, police.getRole())
                .claim(LEVEL, police.getLevel())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTimeAccessToken))
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .compact();

        // Trả về access-token kèm thời gian mà access-token có hiệu lực
        return Map.of(ACCESS_TOKEN, accessToken, ACCESS_TOKEN_EXPIRY, expireTimeAccessToken);
    }

    private Map<String, Object> generateRefreshToken(Police police) {
        // Tìm kiếm refresh-token của tài khoản đang login trong bảng refresh_token
        RefreshToken refreshToken = refreshTokenRepository.findByPoliceId(police.getId()).orElse(null);

        // Nếu không tìm thấy thì tạo bản ghi refresh_token mới
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setPoliceId(police.getId()); // ID cảnh sát gắn với refresh-token
        }

        refreshToken.setToken(UUID.randomUUID().toString());                                 // Dữ liệu refresh-token
        refreshToken.setExpiryDate(new Date(new Date().getTime() + expireTimeRefreshToken)); // Thời gian hiệu lực

        // Lưu vào bảng refresh_token
        refreshTokenRepository.save(refreshToken);

        // Trả về refresh-token kèm thời gian mà refresh-token có hiệu lực
        return Map.of(REFRESH_TOKEN, refreshToken.getToken(), REFRESH_TOKEN_EXPIRY, expireTimeRefreshToken);
    }

    public SuccessResponse<Object> changePassword(ChangePasswordRequest request) {
        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Tìm kiếm thông tin cảnh sát đang đăng nhập trong bảng polices
        // Nếu không tìm thấy hoặc thông tin cảnh sát đã bị xóa => Ném ra lỗi
        Police police = policeRepository
                .findByIdAndStatus(loggedAccount.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        // Kiểm tra mật khẩu truyền cũ truyền xuống có khớp với mật khẩu đang lưu trong bảng polices
        // Bằng cách băm mật khẩu cũ truyền xuống và so mã băm đó với mã băm trong bảng polices
        // Nếu 2 mã băm khác nhau thì ném ra lỗi
        if (!passwordEncoder.matches(request.getOldPassword(), police.getPassword())) {
            throw new ProcessException(WRONG_OLD_PASSWORD);
        }

        // Mã hóa mật khẩu mới bằng cách băm
        String encode = passwordEncoder.encode(request.getNewPassword());

        // Cập nhật mật khẩu mới vào thông tin cảnh sát trong bảng polices
        police.setPassword(encode);
        policeRepository.save(police);

        // Trả về thành công
        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> forgetPassword(String identifyNumber) {
        // Tìm kiếm thông tin cảnh sát trong bảng polices theo số CCCD truyền xuống
        // Nếu không tìm thấy hoặc thông tin cảnh sát đã bị xóa => Ném ra lỗi
        Police police = policeRepository
                .findByIdentifyNumberAndStatus(identifyNumber, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        // Gọi hàm sinh mật khẩu mới
        String password = FunctionUtils.generatePassword();

        // Mã hóa mật khẩu mới bằng cách băm
        String encode = passwordEncoder.encode(password);

        // Cập nhật mật khẩu mới vào thông tin cảnh sát trong bảng polices
        police.setPassword(encode);
        policeRepository.save(police);

        String message;

        if (Objects.equals(police.getRole(), RoleEnums.SHERIFF.value)) { // Nếu tài khoản đang đăng nhập có vai trò là cảnh sát trưởng

            // Gửi mail thông báo về email của tài khoản
            emailService.sendMailSheriffForgetPassword(police, password);

            // Trả về thông báo thành công dành cho cảnh sát trường
            message = "Hệ thống đã gửi mật khẩu mới về email tài khoản bạn đăng ký. Xin hãy kiểm tra hòm thư";

        } else { // Nếu tài khoản đang đăng nhập có vai trò là cảnh sát

            // Tìm kiếm thông tin cảnh sát trưởng quản lý trực tiếp
            Police sheriff = getSheriff(police);

            // Gửi mail thông báo về email của cảnh sát trưởng quản lý trực tiếp
            emailService.sendMailPoliceForgetPassword(sheriff, police, password);

            // Trả về thông báo thành công dành cho cảnh sát
            message = "Hệ thống đã gửi mật khẩu mới về email tài khoản cảnh sát trưởng quản lý trực tiếp cán bộ. Xin hãy chủ động liên hệ để được lấy mật khẩu mới";
        }

        // Trả về thành công kèm thông báo
        return new SuccessResponse<>(message);
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> getAccountInfo() {
        return new SuccessResponse<>(getLoggedAccount());
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> updateAccount(UpdateAccountRequest request) {

        // Lấy ra thông tin tài khoản đang login
        PoliceDto loggedAccount = getLoggedAccount();

        // Tìm kiếm thông tin cảnh sát trong bảng polices theo số CCCD truyền xuống
        // Nếu không tìm thấy hoặc thông tin cảnh sát đã bị xóa => Ném ra lỗi
        Police police = policeRepository
                .findByIdAndStatus(loggedAccount.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        // Validate thông tin giới tính truyền xuống có trong danh mục quy định ?
        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        // Lấy ra thông tin id địa chính đơn vị đang công tác của cảnh sát
        Long cityId = police.getCityId();
        Long districtId = police.getDistrictId();
        Long wardId = police.getWardId();

        SuccessResponse<Object> response;

        if (Objects.equals(loggedAccount.getRole(), RoleEnums.POLICE.value)) { // Nếu tài khoản đang login có vai trò cảnh sát

            // Tìm kiếm trong bảng police_requests xem cảnh sát có yêu cầu chỉnh sửa thông tin tài khoản nào đang Chờ duyệt hay không ?
            // Nếu không tìm thấy thì tạo yêu cầu mới. Ngược lại nếu có thì cập nhật và ghi đè vào yêu cầu cũ
            PoliceRequest policeRequest = policeRequestRepository
                    .findByPoliceIdAndStatus(police.getId(), StatusEnums.WAIT.name())
                    .orElse(new PoliceRequest());

            policeRequest.setPoliceId(police.getId());                      // ID cảnh sát
            policeRequest.setIdentifyNumber(police.getIdentifyNumber());    // Số CCCD
            policeRequest.setFullName(request.getFullName());               // Họ tên
            policeRequest.setGender(gender.value);                          // Giới tính
            policeRequest.setDateOfBirth(request.getDateOfBirth());         // Ngày sinh
            policeRequest.setPhoneNumber(request.getPhoneNumber());         // Số điện thoại
            policeRequest.setEmail(request.getEmail());                     // Email
            policeRequest.setLevel(police.getLevel());                      // Giữ nguyên cấp bậc tài khoản
            policeRequest.setRole(police.getRole());                        // Giữ nguyên vai trò tài khoản
            policeRequest.setCityId(cityId);                                // Giữ nguyên id tỉnh thành phố đơn vị công tác
            policeRequest.setDistrictId(districtId);                        // Giữ nguyên id quận huyện đơn vị công tác
            policeRequest.setWardId(wardId);                                // Giữ nguyên id phường xã đơn vị công tác
            policeRequest.setStatus(StatusEnums.WAIT.name());               // Trạng thái Chờ duyệt (WAIT)

            // Nếu có dữ liệu avatar truyền xuống thì set vào yêu cầu
            if (StringUtils.isNotBlank(request.getAvatar())) {
                policeRequest.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
            } else { // Set vào yêu cầu avatar hiện tại của cảnh sát
                policeRequest.setAvatar(police.getAvatar());
            }

            // Lưu yêu cầu vào bảng police_requests
            policeRequest = policeRequestRepository.save(policeRequest);

            // Trả về thành công kèm dữ liệu bản ghi police_requests vừa tạo
            response = new SuccessResponse<>(convertToPoliceRequestDto(policeRequest));

        } else {

            // Nếu có dữ liệu avatar truyền xuống thì cập nhật vào thông tin tài khoản cảnh sát
            if (StringUtils.isNotBlank(request.getAvatar())) {
                police.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
            }

            police.setFullName(request.getFullName());          // Họ tên
            police.setGender(gender.value);                     // Giới tính
            police.setDateOfBirth(request.getDateOfBirth());    // Ngày sinh
            police.setPhoneNumber(request.getPhoneNumber());    // Số điện thoại
            police.setEmail(request.getEmail());                // Email
            police.setLevel(police.getLevel());                 // Giữ nguyên cấp bậc tài khoản
            police.setRole(police.getRole());                   // Giữ nguyên vai trò tài khoản
            police.setCityId(cityId);                           // Giữ nguyên id tỉnh thành phố đơn vị công tác
            police.setDistrictId(districtId);                   // Giữ nguyên id quận huyện đơn vị công tác
            police.setWardId(wardId);                           // Giữ nguyên id phường xã đơn vị công tác

            // Cập nhật thông tin cảnh sát trong bảng polices
            police = policeRepository.save(police);

            // Trả về thành công kèm dữ liệu cảnh sát vừa được cập nhật
            response = new SuccessResponse<>(convertToPoliceDto(police));
        }

        // Trả về response
        return response;
    }
}
