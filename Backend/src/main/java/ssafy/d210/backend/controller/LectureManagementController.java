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
import ssafy.d210.backend.service.LectureManagementService;
import ssafy.d210.backend.service.LectureManagementServiceImpl;

@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
@Tag(name = "LectureManagementController", description = "강의 등록 기능")
public class LectureManagementController {


    private final LectureManagementService lectureManagementService;

    // 강의 등록하기 @PostMapping
    @PostMapping
    @Operation(summary = "강의 등록", description = "미완")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description="미완")
    })
    public ResponseEntity<ResponseSuccessDto<Void>> registerLecture(
            @RequestBody LectureRegisterRequest request
            ) {
                return ResponseEntity.ok(
                        ResponseSuccessDto.<Void>builder()
                                .build()
            );
    }

    // "강의 등록 이메일 찾기" 는 "회원 가입 이메일 중복 확인"입니다.
}
