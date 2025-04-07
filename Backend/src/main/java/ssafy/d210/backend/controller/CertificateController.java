package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.certificate.CertificateRequest;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateToken;
import ssafy.d210.backend.service.CertificateService;

import java.math.BigInteger;
import java.util.List;
//
@RestController
@RequestMapping("api/certificate")
@RequiredArgsConstructor
@Tag(name = "CertificateController", description = "수료증 조회, 자세히보기, 발급 요청")
public class CertificateController {

    private final CertificateService certificateService;

    // 수료증 조회 @GetMapping
    @GetMapping()
    @Operation(summary = "수료증 조회", description = "{userId}가 수강 완료한 강의의 수료증 정보를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수료증 목록 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<CertificateResponse>>> getCertificates(
            @RequestParam("userId") Long userId
    ) {
        return ResponseEntity.ok(certificateService.getCertificates(userId));
    }

    // 수료증 자세히 보기 @GetMapping("/detail")
    @GetMapping("/detail")
    @Operation(summary = "수료증 자세히 보기", description = "선택한 수료증의 상세 정보를 반환한다. NFT 생성이 가능할 만큼 전달됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수료증 자세히 보기 성공")
    })
    public ResponseEntity<ResponseSuccessDto<CertificateDetailResponse>> getCertificatesDetail(
            @RequestParam("userId") Long userId,
            @RequestParam("lectureId") Long lectureId
    ) {
        return ResponseEntity.ok(certificateService.getCertificatesDetail(userId, lectureId));
    }

    // 수료증 발급 요청 @PatchMapping("/lecture/{lectureId}/certification")
    @PatchMapping("/lecture/{lectureId}/certification")
    @Operation(summary = "수료증 발급 요청", description = "{userId}에 {lectureId} 강의에 대한 NFT를 발행한다.\nNFT에 {userId}와 {cid}를 저장한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수료증 발급 요청")
    })
    public ResponseEntity<ResponseSuccessDto<CertificateToken>> issueCertificate(
            @RequestBody CertificateRequest request,
            @PathVariable("lectureId") Long lectureId
    ) {
        BigInteger tokenId = certificateService.issueCertificate(request.getUserId(), request.getCid());
        return ResponseEntity.ok(certificateService.saveCertificate(tokenId, lectureId, request.getUserId()));
    }
}
