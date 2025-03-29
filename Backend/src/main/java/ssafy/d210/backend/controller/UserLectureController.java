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
//
@RestController
@RequestMapping("/api/userlecture")
@RequiredArgsConstructor
@Tag(name = "UserLectureController", description = "미완 유저 보유 강의 및 학습 관련 API")
public class UserLectureController {

    private final UserLectureService userLectureService;

    // 재생 시간 저장 @PostMapping
    @PostMapping("/{userLectureId}/time")
    @Operation(summary = "재생 시간 업데이트", description = "{userLectureId}, subLectureId, continueWatching, endFlag로 재생 시간을 업데이트합니다. <br> endFlag가 true이면 강의를 다 보았다는 뜻입니다. <br>DB에서는 1로 저장을 할 것입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재생 시간 저장 성공")
    })
    public ResponseEntity<ResponseSuccessDto<Boolean>> updateLectureTime(
            @PathVariable Long userLectureId,
            @RequestParam Long subLectureId,
            @RequestBody LectureTimeRequest request
    ) {
        return ResponseEntity.ok(userLectureService.updateLectureTime(userLectureId, subLectureId, request));
    }

}
