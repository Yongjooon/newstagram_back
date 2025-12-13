package com.ssafy.newstagram.api.logging.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ssafy.newstagram.api.logging.service.AopTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/kafka/logging")
@RequiredArgsConstructor
@Tag(name = "Kafka Test API", description = "Kafka 메시지 전송 및 소비 테스트용 API")
public class LogTestController {

    private final AopTestService aopTestService;

    @Operation(summary = "기사 클릭 로그 전송", description = "로그인 없이 특정 유저가 기사를 클릭한 상황을 가정하여 Kafka 메시지를 전송합니다.")
    @PostMapping("/click")
    public String sendTestLog(@RequestParam Long userId, @RequestParam Long articleId) throws JsonProcessingException {
        UsernamePasswordAuthenticationToken fakeAuth =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

        // SecurityContext에 테스트 정보 주입
        SecurityContextHolder.getContext().setAuthentication(fakeAuth);

        try {
            aopTestService.testMethodForAop(articleId);
            return "Aspect 테스트 성공! (SecurityContext에 유저 " + userId + " 주입 완료)";
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}