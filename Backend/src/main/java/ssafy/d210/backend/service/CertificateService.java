package ssafy.d210.backend.service;
//
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateToken;

import java.math.BigInteger;
import java.util.List;


public interface CertificateService {

    // 수료증 전체 목록 조회
    public ResponseSuccessDto<List<CertificateResponse>> getCertificates(Long userId);

    // 수료증 자세히 보기
    public ResponseSuccessDto<CertificateDetailResponse> getCertificatesDetail(Long userId, Long lectureId);

    // 수료증 발급
    /*
    *   Front에서 저장, 서명 후 요청 정보 전달
    *   Back에서 transaction 실행 후 결과 값(tokenId) 저장
    *   저장한 tokenId와 생성한 qrCode 반환
    */
    public BigInteger issueCertificate(Long userId, String cid) throws Exception;

    // 수료증 저장하기
    public ResponseSuccessDto<CertificateToken> saveCertificate(BigInteger tokenId, Long lectureId, Long userId, String cid);
}
