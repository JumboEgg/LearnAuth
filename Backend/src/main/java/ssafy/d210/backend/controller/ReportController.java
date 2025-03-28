package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.report.ReportRequest;
import ssafy.d210.backend.dto.response.report.ReportDetailResponse;
import ssafy.d210.backend.dto.response.report.ReportResponse;
import ssafy.d210.backend.security.dto.CustomUserDetails;
import ssafy.d210.backend.service.ReportService;
import ssafy.d210.backend.service.ReportServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "ReportController", description = "신고 관련 API")
public class ReportController {

    private final ReportService reportService;

    // 받은 신고 전체 조회 @GetMapping
    @GetMapping
    @Operation(summary = "받은 신고 전체 조회", description = "사용자의 전체 신고 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 목록 조회 성공")
    })
    // userId를 통해 해당 유저의 신고 목록 반환하는 메서드 : getAllReports
    public ResponseEntity<ResponseSuccessDto<List<ReportResponse>>> getAllReports(
            // 한나 : jwt 구현 후면 param으로 userId를 받을 필요가 있을까?
            // @AuthenticationPrincipal CustomUserDetails userDetails
            @RequestParam long userId
    ) {
        // jwt : LonguserId = userDetails.getUserId();
        // reportService에서 userId를 갖고 해당 유저의 신고 리스트를 가지고 온다.
        List<ReportResponse> reports = reportService.getReports(userId);
        // 조회한 신고 리스트를 응답으로 반환한다.
        return ResponseEntity.ok(
                ResponseSuccessDto.<List<ReportResponse>>builder()
                        .data(reports)
                        .build()
        );
    }

    // 받은 신고 자세히 보기 @GetMapping("/{reportId}")
    @GetMapping("/{reportId}")
    @Operation(summary = "받은 신고 자세히 보기", description = "특정 신고의 상세 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 상세 조회 성공")
    })
    // PathVariable에서 받은 reportId 파라미터로 받아서 처리
    public ResponseEntity<ResponseSuccessDto<ReportDetailResponse>> getReportDetail(
            @PathVariable Long reportId
    ) {
        // 서비스에 해당 reportId 신고 데이터 요청, 데이터를 detail 변수에 담는다.
        ReportDetailResponse detail = reportService.getReportDetail(reportId);
        return ResponseEntity.ok(
                ResponseSuccessDto.<ReportDetailResponse>builder()
                        .data(detail)
                        .build()
        );
    }

    // 신고 하기 @PostMapping
    @PostMapping
    @Operation(summary = "신고하기", description = "강의를 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 접수 성공")
    })
    public ResponseEntity<ResponseSuccessDto<Void>> submitReport(
            @RequestBody ReportRequest request,
            // JWT에서 가져온 사용자 정보
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // userId 꺼내기
        // Long userId = userDetails.getUserId();
        Long userId = 1L;
        reportService.createReport(request, userId);
        return ResponseEntity.ok(
                ResponseSuccessDto.<Void>builder()
                        .status("신고가 접수되었습니다.")
                        .build()
        );
    }


}
