package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.report.ReportRequest;
import ssafy.d210.backend.dto.response.report.ReportDetailResponse;
import ssafy.d210.backend.dto.response.report.ReportResponse;
import ssafy.d210.backend.service.ReportService;
import ssafy.d210.backend.service.ReportServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "ReportController", description = "신고 관련 API")
public class ReportController {

    private final ReportService reportService;

    // 신고 하기 @PostMapping
    @PostMapping
    @Operation(summary = "신고하기", description = "미 완 강의를 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미 완 미 완")
    })
    public ResponseEntity<ResponseSuccessDto<Void>> submitReport(
            @RequestBody ReportRequest request
    ) {
        return ResponseEntity.ok(
                ResponseSuccessDto.<Void>builder()
                        .build()
        );
    }

    // 신고 내역 전체 조회 @GetMapping
    @GetMapping
    @Operation(summary = "신고 내역 전체 조회", description = "미 완 사용자의 전체 신고 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미 완 신고 목록 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<ReportResponse>>> getAllReports(
            @RequestParam long userId
    ) {
        return ResponseEntity.ok(
                ResponseSuccessDto.<List<ReportResponse>>builder()
                        .build()
        );
    }

    // 신고 자세히 보기 @GetMapping("/{reportId}")
    @GetMapping("/{reportId}")
    @Operation(summary = "신고 상세 보기", description = "미 완 미 완 특정 신고의 상세 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미 완 미 완 신고 상세 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<ReportDetailResponse>> getReportDetail(
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(
                ResponseSuccessDto.<ReportDetailResponse>builder()
                        .build()
        );
    }


}
