package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureTimeRequest;
import ssafy.d210.backend.dto.response.lecture.LectureResponse;
import ssafy.d210.backend.service.UserLectureService;

import java.util.List;

@RestController
@RequestMapping("/api/userlecture")
@RequiredArgsConstructor
@Tag(name = "UserLectureController", description = "미완 유저 보유 강의 및 학습 관련 API")
public class UserLectureController {

    private final UserLectureService userLectureService;
    // 내가 보유, 참여한 강의 @GetMapping
    @GetMapping
    @Operation(summary = "보유/참여 강의 목록 조회", description = "미 완 사용자가 보유하거나 참여한 강의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미 완 강의 목록 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<LectureResponse>>> getUserLectures(
            @RequestParam Long userId,
            @RequestParam boolean participant
    ) {
        return ResponseEntity.ok(
                ResponseSuccessDto.<List<LectureResponse>>builder()
                        .build()
        );
    }

    // 재생 시간 저장 @PostMapping
    @PatchMapping("/{userLectureId}/time")
    @Operation(summary = "재생 시간 업데이트", description = "개별 강의의 이어보기 시간을 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재생 시간 저장 성공")
    })
    public ResponseEntity<ResponseSuccessDto<Void>> updateLectureTime(
            @PathVariable Long userLectureId,
            @RequestParam Long subLectId,
            @RequestBody LectureTimeRequest request
    ) {
        return ResponseEntity.ok(
                ResponseSuccessDto.<Void>builder()
                        .build()
        );
    }

}
