package ssafy.d210.backend.service;
//
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.DefaultException;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CertificateServiceImpl implements CertificateService{
    private final UserLectureRepository userLectureRepository;
    private final ResponseUtil responseUtil;

    @Override
    @Transactional(readOnly = true)
    public ResponseSuccessDto<List<CertificateResponse>> getCertificates(Long userId) {
        // 사용자 ID로 완료된 강의 조회
        List<CertificateResponse> certificates = userLectureRepository.getFinishedUserLecture(userId);
        log.info("User {} certificates: {}", userId, certificates);

        // 결과 검증 및 로깅
        if (certificates.isEmpty()) {
            log.warn("No certificates found for user {}", userId);
        }

        ResponseSuccessDto<List<CertificateResponse>> res = responseUtil.successResponse(certificates, HereStatus.SUCCESS_CERTIFICATE);
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseSuccessDto<CertificateDetailResponse> getCertificatesDetail(Long userId, Long lectureId) {
        // 이수증 상세 정보 조회
        CertificateDetailResponse certificateDetail = userLectureRepository.getCertificateDetail(userId, lectureId);

        // 조회 결과 null 체크
        if (certificateDetail == null) {
            log.error("No certificate detail found for user {} and lecture {}", userId, lectureId);
        } else {
            log.info("Certificate Detail: title={}, teacherName={}, certificateDate={}",
                    certificateDetail.getTitle(),
                    certificateDetail.getTeacherName(),
                    certificateDetail.getCertificateDate());
        }

        ResponseSuccessDto<CertificateDetailResponse> res = responseUtil.successResponse(certificateDetail, HereStatus.SUCCESS_CERTIFICATE_DETAIL);
        return res;
    }

    @Override
    public ResponseSuccessDto<Boolean> issueCertificate(Long userId, String cid) {
        return null;
    }
}
