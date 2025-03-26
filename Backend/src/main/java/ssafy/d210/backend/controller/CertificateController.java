package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.dto.response.common.IsValidResponse;
import ssafy.d210.backend.service.CertificateService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/certificate")
@RequiredArgsConstructor
@Tag(name = "CertificateController", description = "수료증 조회, 자세히보기, 발급 요청")
public class CertificateController {

    private final CertificateService certificateService;

    // 수료증 조회 @GetMapping
    @GetMapping("")
    @Operation(summary = "수료증 조회", description = "미완료 상태")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수료증 목록 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<CertificateResponse>> getCertificates(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok().body(
                ResponseSuccessDto.<CertificateResponse>builder()
                        // 아무튼 뼈대
                        .build()
        );
    }

    // 수료증 자세히 보기 @GetMapping("/detail")
    @GetMapping("/detail")
    @Operation(summary = "수료증 자세히 보기", description = "미완료")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수료증 자세히 보기 성공")
    })
    public ResponseEntity<ResponseSuccessDto<CertificateDetailResponse>> getCertificatesDetail(
            @RequestParam Long userId,
            @RequestParam Long lectureId
    ) {

        return ResponseEntity.ok().body(
                ResponseSuccessDto.<CertificateDetailResponse>builder()
                        .build()
        );
    }


    // 수료증 발급 요청 @PatchMapping("/lecture/{lectureId}/certification")
    @PatchMapping("/lecture/{lectureId}/certification")
    @Operation(summary = "수료증 발급 요청", description = "미완료")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수료증 발급 요청")
    })
    public ResponseEntity<IsValidResponse> issueCertificate(
            @RequestParam Long userId,
            @PathVariable Long lectureId
    ) {
        boolean isValid = true; // 일단 냅다 넣어놓음

        return ResponseEntity.ok(new IsValidResponse(isValid));
    }
}
