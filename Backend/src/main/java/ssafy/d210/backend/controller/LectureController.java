package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.LectureRequest;
import ssafy.d210.backend.dto.request.lecture.MainLectureRequest;
import ssafy.d210.backend.dto.request.lecture.PurchaseLectureRequest;
import ssafy.d210.backend.dto.response.common.IsValidResponse;
import ssafy.d210.backend.dto.response.lecture.*;
import ssafy.d210.backend.service.LectureService;

@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
@Tag(name = "LectureController", description = "전체 강의 조회, 조건에 따른 강의 조회, 강의 상세 정보 조회, 강의 검색, 강의 구매")
public class LectureController {

    private final LectureService lectureService;

    //전체 강의 조회 @GetMapping
    @GetMapping("")
    @Operation(summary = "전체 강의 조회", description = "미완료 상태")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 강의 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<AllLectureResponse>> getAllLectures() {
        return ResponseEntity.ok().body(
                ResponseSuccessDto.<AllLectureResponse>builder()
                        .build()
        );
    }

    //조건에 따른 강의 조회 @GetMapping("/recommendation")
    @GetMapping("/recommendation")
    @Operation(summary = "조건에 따른 강의 조회", description = "미완료")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조건 강의 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<RecommendationMainLectureResponse>> getRecommendedLectures(
            RecommendationLectureResponse recommendationLectureResponse
    ) {
        return ResponseEntity.ok().body(
                ResponseSuccessDto.<RecommendationMainLectureResponse>builder()
                        .build()
        );
    }

    //강의 상세 정보 조회, 마이페이지 강의 조회 @GetMapping("/{lectureId})
    @GetMapping("/{lectureId}")
    @Operation(summary = "강의 상세 정보 조회", description = "미완료")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아무튼 미완료")
    })
    public ResponseEntity<ResponseSuccessDto<LectureDetailResponse>> getLectureDetail(
            @PathVariable Long lectureId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok().body(
                ResponseSuccessDto.<LectureDetailResponse>builder()
                        .build()
        );
    }

    //강의 검색 @GetMapping("/search")
    @GetMapping("/search")
    @Operation(summary = "강의검색", description = "미완")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미완")
    })
    public ResponseEntity<ResponseSuccessDto<LectureSearchResponse>> searchLectures(
            @RequestParam String keyword,
            @RequestParam int page
    ) {
        return ResponseEntity.ok().body(
                ResponseSuccessDto.<LectureSearchResponse>builder()
                        // 헤헤 졸리다.
                        .build()
        );
    }

    //강의 구매 @PostMapping("/purchase")
    @PostMapping("/purchase")
    @Operation(summary = "강의구매", description = "미완")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미완")
    })
    public ResponseEntity<ResponseSuccessDto<Void>> purchaseLecture(
            @RequestBody PurchaseLectureRequest request
    ) {
        return ResponseEntity.ok(
                ResponseSuccessDto.<Void>builder()
//                        .message("강의 구매 성공")
                        // 뭐로 넘겨주게 될지 모르겠지만 여튼.. boolean true?
                        .build()
        );
    }

}
