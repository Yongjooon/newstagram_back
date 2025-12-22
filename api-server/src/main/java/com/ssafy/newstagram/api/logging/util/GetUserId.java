package com.ssafy.newstagram.api.logging.util;

import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class GetUserId {
    public static Long getUserIdFromSecurity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUserId();
            }

            if (principal instanceof Long) {
                return (Long) principal;
            }

            if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("[UserId Extraction] ID 파싱 실패 - 입력값: '{}' (Type: String)", principal);
                    return null;
                }
            }

        } catch (Exception e) {
            log.error("[UserId Extraction] 알 수 없는 에러 발생 - Authentication: {}", SecurityContextHolder.getContext().getAuthentication(), e);
        }

        return null;
    }
}
