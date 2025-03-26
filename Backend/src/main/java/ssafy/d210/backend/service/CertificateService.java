package ssafy.d210.backend.service;

import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;

import java.util.List;

@Service
public interface CertificateService {

    // 아무튼 수료증 조회
    public List<CertificateResponse> getCertificates(Long userId, Long lectureId);

    // 아무튼 수료증 자세히 보기 확인 필요
    public List<CertificateDetailResponse> getCertificatesDetail();

    // 아무튼 수료증 발급
    public CertificateResponse issueCertificate(Long lectureId, Long userId);
}
