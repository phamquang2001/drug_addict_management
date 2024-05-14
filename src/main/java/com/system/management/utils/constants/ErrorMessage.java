package com.system.management.utils.constants;

public class ErrorMessage {

    public static final String ACCOUNT_NOT_EXISTS = "Không tìm thấy thông tin tài khoản";
    public static final String WRONG_PASSWORD = "Mật khẩu không đúng";
    public static final String WRONG_OLD_PASSWORD = "Mật khẩu cũ không đúng";
    public static final String INVALID_ACCESS_TOKEN = "Access token không hợp lệ";
    public static final String INVALID_REFRESH_TOKEN = "Refresh token không hợp lệ";
    public static final String BLOCKED_ACCESS_TOKEN = "Access token đã bị chặn";
    public static final String EXPIRED_ACCESS_TOKEN = "Access token đã hết hạn";
    public static final String VERIFY_TOKEN_FAILED = "Xác thực token thất bại";
    public static final String CITY_NOT_EXISTS = "Tỉnh/Thành phố không tồn tại";
    public static final String CITY_EXISTS_WITH_CODE = "Đã tồn tại tỉnh thành phố có mã truyền xuống";
    public static final String DISTRICT_NOT_EXISTS = "Quận/Huyện không tồn tại";
    public static final String DISTRICT_EXISTS_WITH_CODE = "Trong tỉnh thành phố đã tồn tại quận huyện có mã truyền xuống";
    public static final String WARD_NOT_EXISTS = "Phường/Xã không tồn tại";
    public static final String WARD_EXISTS_WITH_CODE = "Trong quận huyện đã tồn tại phường xã có mã truyền xuống";
    public static final String POLICE_NOT_EXISTS = "Cảnh sát không tồn tại";
    public static final String DRUG_ADDICT_NOT_EXISTS = "Đối tượng không tồn tại";
    public static final String REQUEST_NOT_EXISTS = "Yêu cầu không tồn tại";
    public static final String TREATMENT_PLACE_NOT_EXISTS = "Nơi cai nghiện không tồn tại";
    public static final String SHERIFF_NOT_EXISTS = "Không tìm thấy thông tin cảnh sát trưởng";
    public static final String NOT_ALLOW = "Tài khoản không được phép thực hiện yêu cầu này";
    public static final String INVALID_IDENTIFY_NUMBER = "Số cccd đã có thông tin trong hệ thống";
    public static final String INVALID_GENDER = "Giới tính không hợp lệ! Vui lòng nhập 1 (Nam) hoặc 2 (Nữ)";
    public static final String INVALID_LEVEL = "Cấp bậc tài khoản không hợp lệ";
    public static final String INVALID_ROLE = "Vai trò tài khoản không hợp lệ";
    public static final String INVALID_STATUS = "Trạng thái không hợp lệ";
    public static final String REASON_REJECTED_REQUIRED = "Yêu cầu phải có lý do từ chối";

    private ErrorMessage() {
    }

}
