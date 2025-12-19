package com.ssafy.newstagram.api.survey.controller;

import com.ssafy.newstagram.api.common.BaseResponse;
import com.ssafy.newstagram.api.survey.model.dto.SurveySubmitRequestDto;
import com.ssafy.newstagram.api.survey.model.service.SurveyService;
import com.ssafy.newstagram.api.users.model.dto.CustomUserDetails;
import com.ssafy.newstagram.api.users.model.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/survey")
@RequiredArgsConstructor
@Tag(name = "Survey API", description = "최초 로그인 시 콜드 스타트용 임베딩 데이터 설문")
public class SurveyController {

    private final UserService userService;
    private final SurveyService surveyService;

    @GetMapping("/categories")
    @Operation(
            summary = "설문용 카테고리 목록 조회",
            description = "TOP(1)과 OTHER(17)를 제외한 카테고리 목록을 반환합니다."
    )
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "SURVEY_200",
                        "카테고리 목록 조회 성공",
                        surveyService.getSurveyCategories()
                )
        );
    }

    @PostMapping("/submit")
    @Operation(
            summary = "설문 결과 제출",
            description = "선택한 카테고리들을 기반으로 초기 추천 벡터를 생성합니다."
    )
    public ResponseEntity<?> submitSurvey(@RequestBody SurveySubmitRequestDto request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        surveyService.processSurvey(userDetails.getUserId(), request.getCategoryIds());
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.success(
                        "SURVEY_200",
                        "관심사 설정 완료",
                        "초기 설정이 완료되었습니다. 메인 페이지로 이동하세요."
                )
        );
    }
}
