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
@Tag(name = "LectureManagementController", description = "[미완] 강의 등록 기능")
public class LectureManagementController {


    private final LectureManagementService lectureManagementService;

    // 강의 등록하기 @PostMapping
    @PostMapping
    @Operation(summary = "강의 등록", description = "미완")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description="강의 등록 성공")
    })

    // request를 받아서 처리
    public ResponseEntity<ResponseSuccessDto<Boolean>> registerLecture(
            @RequestBody LectureRegisterRequest request
    ) {
        // 로직 실행
        boolean result = lectureManagementService.registerLecture(request);
        // 이 부분 형식 맞추기 필요
        return ResponseEntity.ok(
                ResponseSuccessDto.<Boolean>builder()
                    .data(result)
                    .timestamp(ZonedDateTime.now())
                    .code(200)
                    .status("ok")
                    .build()
        );
    }

    // "강의 등록 이메일 찾기" 는 "회원 가입 이메일 중복 확인"입니다.
}
