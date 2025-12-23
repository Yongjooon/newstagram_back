package com.ssafy.newstagram.api.logging.aspect;

import com.ssafy.newstagram.api.logging.util.GetUserId;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("execution(* com.ssafy.newstagram..controller..*(..))")
    public void apiController() {}

    @Around("apiController()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        String defaultValue = "unknown";
        long start = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String method = request != null ? request.getMethod() : defaultValue;
        String uri = request != null ? request.getRequestURI() : defaultValue;
        Long userId = null;
        if(!uri.equals("/api/auth/token")) {
            userId = request != null ? GetUserId.getUserIdFromSecurity() : null;
        }
        String clientIp = request != null ? request.getRemoteAddr() : defaultValue;

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            log.info("[API] Success - traceId={} method={} uri={} status=200 time={}ms userId={} clientIp={}", traceId, method, uri, executionTime, userId, clientIp);
            return result;
        } catch(Exception e) {
            long executionTime = System.currentTimeMillis() - start;
            log.error("[API] Error - traceId={} method={} uri={} status=500 time={}ms  userId={} clientIp={} message={}", traceId, method, uri, executionTime, userId, clientIp, e.getMessage());
            throw e;
        }
    }

}
