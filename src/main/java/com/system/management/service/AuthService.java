package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.BlackList;
import com.system.management.model.entity.Police;
import com.system.management.model.entity.RefreshToken;
import com.system.management.model.request.auth.LoginRequest;
import com.system.management.model.response.SuccessResponse;
import com.system.management.repository.BlackListRepository;
import com.system.management.repository.PoliceRepository;
import com.system.management.repository.RefreshTokenRepository;
import com.system.management.utils.AESUtils;
import com.system.management.utils.enums.LevelEnums;
import com.system.management.utils.enums.RoleEnums;
import com.system.management.utils.exception.BadRequestException;
import com.system.management.utils.exception.ProcessException;
import com.system.management.utils.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

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
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final PoliceRepository policeRepository;
    private final BlackListRepository blackListRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${config.jwt_key}")
    private String jwtKey;
    @Value("${config.expire_time.access_token}")
    private Long expireTimeAccessToken;
    @Value("${config.expire_time.refresh_token}")
    private Long expireTimeRefreshToken;

    public SuccessResponse<Object> login(LoginRequest request) {
        Police police = policeRepository
                .findByIdentifyNumber(request.getUsername())
                .orElseThrow(() -> new ProcessException("Không tìm thấy thông tin cán bộ theo số cccd"));

        if (!passwordEncoder.matches(request.getPassword(), police.getPassword())) {
            throw new ProcessException("Mật khẩu không đúng");
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
            throw new BadRequestException("Access token không hợp lệ");
        }

        token = token.replace("Bearer", "").trim();

        if (blackListRepository.existsByToken(token)) {
            throw new ProcessException("Token đã bị chặn");
        }

        Jws<Claims> claimsJws;

        try {
            claimsJws = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(token);
        } catch (ExpiredJwtException ex) {
            throw new UnauthorizedException(504, "Token đã hết hạn");
        } catch (Exception ex) {
            throw new UnauthorizedException("Xác thực token thất bại");
        }

        Police police = policeRepository
                .findByIdentifyNumber(claimsJws.getBody().getSubject())
                .orElseThrow(() -> new ProcessException("Không tìm thấy thông tin cán bộ theo số cccd"));

        return new SuccessResponse<>(modelMapper.map(police, PoliceDto.class));
    }

    public SuccessResponse<Object> refresh(String token) {
        String decryptToken = aesUtils.decrypt(token);

        RefreshToken oldRefreshToken = refreshTokenRepository
                .findByToken(decryptToken)
                .orElseThrow(() -> new ProcessException("Refresh token không hợp lệ"));

        Police police = policeRepository
                .findById(oldRefreshToken.getPoliceId())
                .orElseThrow(() -> new ProcessException("Không tìm thấy thông tin cán bộ theo id"));

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
            throw new BadRequestException("Access token không hợp lệ");
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
}
