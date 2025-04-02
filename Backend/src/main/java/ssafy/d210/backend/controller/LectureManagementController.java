package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureRegisterRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.service.LectureManagementService;
import ssafy.d210.backend.service.LectureManagementServiceImpl;

import java.time.ZonedDateTime;

//
@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
@Tag(name = "LectureManagementController", description = "강의 등록 기능")
public class LectureManagementController {


    private final LectureManagementService lectureManagementService;

    // 강의 등록하기 @PostMapping
    @PostMapping
    @Operation(summary = "강의 등록", description = """
            - input : 강의 제목, 카테고리, 목표, 설명, 금액, 정산 비율, 세부 강의, 퀴즈
            - output : 성공 시 true 반환
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description="강의 등록 성공")
    })

    // request를 받아서 처리
    public ResponseEntity<ResponseSuccessDto<Boolean>> registerLecture(
            @RequestBody LectureRegisterRequest request
    ) {
        return ResponseEntity.ok(lectureManagementService.registerLecture(request));
    }

    // "강의 등록 이메일 찾기" 는 "회원 가입 이메일 중복 확인"입니다.
}
