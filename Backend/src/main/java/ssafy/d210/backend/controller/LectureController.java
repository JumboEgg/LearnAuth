package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.lecture.PurchaseLectureRequest;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;
import ssafy.d210.backend.dto.response.lecture.*;
import ssafy.d210.backend.service.LectureService;
import ssafy.d210.backend.service.MetaTransactionService;

import java.util.List;
//
@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
@Tag(name = "LectureController", description = "전체 강의 조회, 조건에 따른 강의 조회, 강의 상세 정보 조회, 강의 검색, 강의 구매")
public class LectureController {

    private final LectureService lectureService;
    private final MetaTransactionService metaTransactionService;

    //전체 강의 조회 @GetMapping
    @GetMapping("")
    @Operation(summary = "카테고리별 강의 조회", description = "전체 및 카테고리별 강의 목록 반환. {page} 기준으로 12개씩 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 강의 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<LectureInfoListResponse>>> getLecturesByCategory(
            @RequestParam("categoryId") int categoryId,
            @RequestParam("page") int page
    ) {
        return ResponseEntity.ok(lectureService.getLecturesByCategory(categoryId, page));
    }

    //조건에 따른 강의 조회 @GetMapping("/recommendation")
    @GetMapping("/recommendation")
    @Operation(summary = "메인 페이지 강의 조회", description = "가장 많은 사람이 이수한 강의 3개, 무작위 강의 10개, 최신 강의 10개를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조건 강의 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<RecommendedLectureResponse>> getRecommendedLectures() {
        return ResponseEntity.ok(lectureService.getRecommendedLectures());
    }

    // 최다 이수 강의
    @GetMapping("/mostCompleted")
    @Operation(summary = "최다 강의 목록 조회", description = "가장 많은 사람이 듣는 강의를 최대 3개 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최다 강의 목록 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<LectureInfoListResponse>>> getMostCompletedLectures() {
        return ResponseEntity.ok(lectureService.getMostCompletedLectures());
    }

    // 최다 이수 강의
    @GetMapping("/random")
    @Operation(summary = "무작위 강의 목록 조회", description = "무작위 강의를 최대 10개 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "무작위 강의 목록 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<LectureInfoListResponse>>> getRamdomLectures() {
        return ResponseEntity.ok(lectureService.getRandomLectures());
    }

    // 최다 이수 강의
    @GetMapping("/mostRecent")
    @Operation(summary = "최근 강의 목록 조회", description = "가장 최근에 등록된 강의를 최대 10개 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 강의 목록 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<LectureInfoListResponse>>> getMostRecentLectures() {
        return ResponseEntity.ok(lectureService.getMostRecentLectures());
    }

    //강의 상세 정보 조회, 마이페이지 강의 조회 @GetMapping("/{lectureId})
    @GetMapping("/{lectureId}")
    @Operation(summary = "강의 상세 정보 조회", description = "lectureId와 userId를 통해 강의 상세를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "lecture, userLecture, subLecture, userLectureTime을 가져옵니다.")
    })
    public ResponseEntity<ResponseSuccessDto<LectureDetailResponse>> getLectureDetail(
            @PathVariable("lectureId") Long lectureId,
            @RequestParam("userId") Long userId
    ) {
        return ResponseEntity.ok(lectureService.getLectureDetail(lectureId, userId));
    }

    //강의 검색 @GetMapping("/search")
    @GetMapping("/search")
    @Operation(summary = "강의 검색", description = """
            - input : keyword, page (Query Parameter)
            - output : 1 page 당 최대 12개의 강의 검색 결과 반환
                - lectureId, title, price, lecturer, lectureUrl, categoryName
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 검색 성공")
    })
    public ResponseEntity<ResponseSuccessDto<LectureSearchResponse>> searchLectures(
            @RequestParam("keyword") String keyword,
            @RequestParam("page") int page
    ) {
        return ResponseEntity.ok(lectureService.searchLectures(keyword, page));
    }

    //강의 구매 @PostMapping("/purchase")
    @PostMapping("/purchase")
    @Operation(summary = "강의구매", description = "유저 ID와 강의 ID를 통해 강의를 구매한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "강의 구매 완료")
    })
    public ResponseEntity<ResponseSuccessDto<Object>> purchaseLecture(
            @RequestBody PurchaseLectureRequest request
    ) {
        if (metaTransactionService.executeMetaTransaction(request.getApproveRequest()))
            if (metaTransactionService.executeMetaTransaction(request.getPurchaseRequest()))
                return ResponseEntity.ok(lectureService.purchaseLecture(request.getUserId(), request.getLectureId()));
        return ResponseEntity.ok(ResponseSuccessDto.builder().data(false).build());
    }

    // 내가 보유한 강의
    @GetMapping("/owned")
    @Operation(summary = "내가 보유한 강의", description = "{userId}가 보유한 강의 목록을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 반환값입니다.")
    })
    public ResponseEntity<ResponseSuccessDto<List<LectureResponse>>> getPurchasedLectures(
            @RequestParam("userId") Long userId
    ) {
        return ResponseEntity.ok(lectureService.getPurchasedLectures(userId));
    }

    // 내가 참여한 강의
    @GetMapping("/participated")
    @Operation(summary = "내가 참여한 강의", description = "{userId}가 참여한 강의 목록을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "임시 반환값입니다.")
    })
    public ResponseEntity<ResponseSuccessDto<List<LectureResponse>>> getParticipatedLectures(
            @RequestParam("userId") Long userId
    ) {
        return ResponseEntity.ok(lectureService.getParticipatedLectures(userId));
    }

}
