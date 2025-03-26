package ssafy.d210.backend.service;

import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;

import java.util.List;

public class CertificateServiceImpl implements CertificateService{
    @Override
    public List<CertificateResponse> getCertificates(Long userId, Long lectureId) {
        return null;
    }

    @Override
    public List<CertificateDetailResponse> getCertificatesDetail() {
        return null;
    }

    @Override
    public CertificateResponse issueCertificate(Long lectureId, Long userId) {
        return null;
    }
}
