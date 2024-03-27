package com.system.management.config.filter;

import com.google.gson.Gson;
import com.system.management.model.dto.PoliceDto;
import com.system.management.model.response.ErrorResponse;
import com.system.management.model.response.SuccessResponse;
import com.system.management.service.AuthService;
import com.system.management.utils.exception.UnauthorizedException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class TokenFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final Gson gson;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws IOException {

        int status = HttpStatus.UNAUTHORIZED.value();
        String errorMessage = "Xác thực người dùng thất bại";

        try {
            String token = request.getHeader("Authorization");
            if (StringUtils.isBlank(token)) {
                throw new UnauthorizedException("Token không hợp lệ");
            }

            SuccessResponse<Object> verify = authService.verify(token);

            PoliceDto loggedAccount = (PoliceDto) verify.getData();
            CustomUserDetails userDetails = new CustomUserDetails(loggedAccount);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(loggedAccount);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
            return;
        } catch (UnauthorizedException e) {
            log.error("Filter token error: {}", e.getMessage());
            status = e.getStatus();
            errorMessage = e.getMessage();

        } catch (Exception e) {
            log.error("Filter token error: " + e.getMessage(), e);
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(gson.toJson(new ErrorResponse(status, errorMessage, request.getRequestURI())));
    }
}
