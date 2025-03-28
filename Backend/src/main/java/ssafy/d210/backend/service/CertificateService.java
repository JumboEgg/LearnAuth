package ssafy.d210.backend.service;

import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;

import java.util.List;


public interface CertificateService {

    // 수료증 전체 목록 조회
    public ResponseSuccessDto<List<CertificateResponse>> getCertificates(Long userId);

    // 수료증 자세히 보기
    public ResponseSuccessDto<CertificateDetailResponse> getCertificatesDetail(Long userId, Long lectureId);

    // 수료증 발급
    /*TODO : 수료증 발급 절차 논의
    *  1. Front에서 private key를 관리하는 경우
    *   Front에서 저장, 서명 후 요청 정보 전달
    *   Back에서 transaction 실행 후 결과 값(tokenId) 저장
    *   저장한 tokenId와 생성한 qrCode 반환
    *  2. Back에서 key를 관리하는 경우
    *   Back에서 저장, 서명, 요청 후 저장
    *   Front에 tokenId와 생성한 qrCode 반환
    */
    public ResponseSuccessDto<Boolean> issueCertificate(Long lectureId, Long userId);
}
