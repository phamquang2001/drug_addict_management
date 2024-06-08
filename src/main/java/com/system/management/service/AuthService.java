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
import com.system.management.utils.AESUtils;
import com.system.management.utils.FunctionUtils;
import com.system.management.utils.constants.ErrorMessage;
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
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final AESUtils aesUtils;
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
        Police police = policeRepository
                .findByIdentifyNumberAndStatus(request.getUsername(), StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        if (!passwordEncoder.matches(request.getPassword(), police.getPassword())) {
            throw new ProcessException(WRONG_PASSWORD);
        }

        Map<?, ?> accessToken = generateAccessToken(police);

        Map<?, ?> refreshToken = generateRefreshToken(police);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(USERNAME, request.getUsername());
        data.put(ROLE, police.getRole());
        data.put(ROLE_NAME, RoleEnums.dict.get(police.getRole()).label);
        data.put(LEVEL, police.getLevel());
        data.put(LEVEL_NAME, LevelEnums.dict.get(police.getLevel()).label);
        data.put(ACCESS_TOKEN, accessToken.get(ACCESS_TOKEN));
        data.put(ACCESS_TOKEN_EXPIRY, accessToken.get(ACCESS_TOKEN_EXPIRY));
        data.put(REFRESH_TOKEN, refreshToken.get(REFRESH_TOKEN));
        data.put(REFRESH_TOKEN_EXPIRY, refreshToken.get(REFRESH_TOKEN_EXPIRY));

        return new SuccessResponse<>(data);
    }

    public SuccessResponse<Object> verify(String token) {
        if (StringUtils.isBlank(token)) {
            throw new BadRequestException(INVALID_ACCESS_TOKEN);
        }

        token = token.replace("Bearer", "").trim();

        if (blackListRepository.existsByToken(token)) {
            throw new ProcessException(BLOCKED_ACCESS_TOKEN);
        }

        Jws<Claims> claimsJws;

        try {
            claimsJws = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(token);
        } catch (ExpiredJwtException ex) {
            throw new UnauthorizedException(504, EXPIRED_ACCESS_TOKEN);
        } catch (Exception ex) {
            throw new UnauthorizedException(VERIFY_TOKEN_FAILED);
        }

        Police police = policeRepository
                .findByIdentifyNumberAndStatus(claimsJws.getBody().getSubject(), StatusEnums.ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        return new SuccessResponse<>(convertToPoliceDto(police));
    }

    public SuccessResponse<Object> refresh(String token) {
        String decryptToken = aesUtils.decrypt(token);

        RefreshToken oldRefreshToken = refreshTokenRepository
                .findByToken(decryptToken)
                .orElseThrow(() -> new ProcessException(INVALID_REFRESH_TOKEN));

        Police police = policeRepository
                .findById(oldRefreshToken.getPoliceId())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        Map<?, ?> accessToken = generateAccessToken(police);

        Map<?, ?> refreshToken = generateRefreshToken(police);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(USERNAME, police.getIdentifyNumber());
        data.put(ROLE, police.getRole());
        data.put(ROLE_NAME, RoleEnums.dict.get(police.getRole()).label);
        data.put(LEVEL, police.getLevel());
        data.put(LEVEL_NAME, LevelEnums.dict.get(police.getLevel()).label);
        data.put(ACCESS_TOKEN, accessToken.get(ACCESS_TOKEN));
        data.put(ACCESS_TOKEN_EXPIRY, accessToken.get(ACCESS_TOKEN_EXPIRY));
        data.put(REFRESH_TOKEN, refreshToken.get(REFRESH_TOKEN));
        data.put(REFRESH_TOKEN_EXPIRY, refreshToken.get(REFRESH_TOKEN_EXPIRY));

        return new SuccessResponse<>(data);
    }

    public SuccessResponse<Object> logout(String token) {
        if (StringUtils.isBlank(token)) {
            throw new BadRequestException(INVALID_ACCESS_TOKEN);
        }

        BlackList blackList = new BlackList();
        blackList.setToken(token.replace("Bearer", "").trim());
        blackList.setBlockDate(new Date());
        blackListRepository.save(blackList);

        PoliceDto loggedAccount =
                (PoliceDto) SecurityContextHolder.getContext().getAuthentication().getDetails();
        refreshTokenRepository.deleteAllByPoliceId(loggedAccount.getId());

        return new SuccessResponse<>();
    }

    private Map<String, Object> generateAccessToken(Police police) {
        Date now = new Date();

        String accessToken = Jwts.builder()
                .setSubject(police.getIdentifyNumber())
                .claim(ROLE, police.getRole())
                .claim(LEVEL, police.getLevel())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTimeAccessToken))
                .signWith(SignatureAlgorithm.HS256, jwtKey)
                .compact();

        return Map.of(ACCESS_TOKEN, accessToken, ACCESS_TOKEN_EXPIRY, expireTimeAccessToken);
    }

    private Map<String, Object> generateRefreshToken(Police police) {
        RefreshToken refreshToken = refreshTokenRepository.findByPoliceId(police.getId()).orElse(null);

        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setPoliceId(police.getId());
        }

        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(new Date(new Date().getTime() + expireTimeRefreshToken));

        refreshTokenRepository.save(refreshToken);

        return Map.of(REFRESH_TOKEN, refreshToken.getToken(), REFRESH_TOKEN_EXPIRY, expireTimeRefreshToken);
    }

    public SuccessResponse<Object> changePassword(ChangePasswordRequest request) {
        PoliceDto loggedAccount =
                (PoliceDto) SecurityContextHolder.getContext().getAuthentication().getDetails();

        Police police = policeRepository
                .findByIdAndStatus(loggedAccount.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        if (!passwordEncoder.matches(request.getOldPassword(), police.getPassword())) {
            throw new ProcessException(WRONG_OLD_PASSWORD);
        }

        police.setPassword(passwordEncoder.encode(request.getNewPassword()));
        policeRepository.save(police);

        return new SuccessResponse<>();
    }

    public SuccessResponse<Object> forgetPassword(String identifyNumber) {
        Police police = policeRepository
                .findByIdentifyNumberAndStatus(identifyNumber, ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        String password = FunctionUtils.generatePassword();
        police.setPassword(passwordEncoder.encode(password));
        policeRepository.save(police);

        String messsage;

        if (Objects.equals(police.getRole(), RoleEnums.SHERIFF.value)) {
            emailService.sendMailSheriffForgetPassword(police, password);
            messsage = "Hệ thống đã gửi mật khẩu mới về email tài khoản bạn đăng ký. Xin hãy kiểm tra hòm thư";
        } else {
            emailService.sendMailPoliceForgetPassword(getSheriff(police), police, password);
            messsage = "Hệ thống đã gửi mật khẩu mới về email tài khoản cảnh sát trưởng quản lý trực tiếp cán bộ. Xin hãy chủ động liên hệ để được lấy mật khẩu mới";
        }

        return new SuccessResponse<>(messsage);
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> getAccountInfo() {
        return new SuccessResponse<>(getLoggedAccount());
    }

    @Transactional(rollbackFor = Exception.class)
    public SuccessResponse<Object> updateAccount(UpdateAccountRequest request) {

        PoliceDto loggedAccount = getLoggedAccount();

        Police police = policeRepository
                .findByIdAndStatus(loggedAccount.getId(), ACTIVE.name())
                .orElseThrow(() -> new ProcessException(ACCOUNT_NOT_EXISTS));

        GenderEnums gender = GenderEnums.dict.get(request.getGender());
        if (gender == null) {
            throw new BadRequestException(INVALID_GENDER);
        }

        Long cityId = police.getCityId();
        Long districtId = police.getDistrictId();
        Long wardId = police.getWardId();

        SuccessResponse<Object> response;

        if (Objects.equals(loggedAccount.getRole(), RoleEnums.POLICE.value)) {

            PoliceRequest policeRequest = new PoliceRequest();
            policeRequest.setPoliceId(police.getId());
            policeRequest.setIdentifyNumber(police.getIdentifyNumber());
            policeRequest.setFullName(request.getFullName());
            policeRequest.setGender(gender.value);
            policeRequest.setDateOfBirth(request.getDateOfBirth());
            policeRequest.setPhoneNumber(request.getPhoneNumber());
            policeRequest.setEmail(request.getEmail());
            policeRequest.setLevel(police.getLevel());
            policeRequest.setRole(police.getRole());
            policeRequest.setCityId(cityId);
            policeRequest.setDistrictId(districtId);
            policeRequest.setWardId(wardId);
            policeRequest.setStatus(StatusEnums.WAIT.name());

            if (StringUtils.isNotBlank(request.getAvatar())) {
                policeRequest.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
            } else {
                policeRequest.setAvatar(police.getAvatar());
            }

            policeRequest = policeRequestRepository.save(policeRequest);

            response = new SuccessResponse<>(convertToPoliceRequestDto(policeRequest));

        } else {

            if (StringUtils.isNotBlank(request.getAvatar())) {
                police.setAvatar(Base64.getDecoder().decode(request.getAvatar()));
            }

            police.setFullName(request.getFullName());
            police.setGender(gender.value);
            police.setDateOfBirth(request.getDateOfBirth());
            police.setPhoneNumber(request.getPhoneNumber());
            police.setEmail(request.getEmail());
            police.setLevel(police.getLevel());
            police.setRole(police.getRole());
            police.setCityId(cityId);
            police.setDistrictId(districtId);
            police.setWardId(wardId);

            police = policeRepository.save(police);

            response = new SuccessResponse<>(convertToPoliceDto(police));
        }

        return response;
    }
}
