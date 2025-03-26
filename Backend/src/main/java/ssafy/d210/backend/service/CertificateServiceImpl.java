package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.response.certificate.CertificateDetailResponse;
import ssafy.d210.backend.dto.response.certificate.CertificateResponse;
import ssafy.d210.backend.exception.DefaultException;
import ssafy.d210.backend.repository.UserLectureRepository;
import java.util.List;

// TODO : ResponseUtil 적용
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService{
    private final UserLectureRepository userLectureRepository;

    @Override
    public List<CertificateResponse> getCertificates(Long userId) {
        // 사용자 ID로 완료된 강의 조회
        List<CertificateResponse> certificates =userLectureRepository.getFinishedUserLecture(userId);
        log.info("User {} certificates: {}", userId, certificates);

        // 결과 검증 및 로깅
        if (certificates.isEmpty()) {
            log.warn("No certificates found for user {}", userId);
        }

        return certificates.stream()
                .map(certificate -> {
                    // 필요하다면 추가 처리 가능 (예: 날짜 포맷팅, 추가 정보 조회 등)
                    log.info("Certificate details: lectureId={}, title={}, category={}",
                            certificate.getLectureId(),
                            certificate.getTitle(),
                            certificate.getCategoryName());

                    return certificate;
                })
                .toList();
    }

    @Override
    public CertificateDetailResponse getCertificatesDetail(Long userId, Long lectureId) {
        // 이수증 상세 정보 조회
        CertificateDetailResponse certificateDetail = userLectureRepository.getCertificateDetail(userId, lectureId);

        // 조회 결과 null 체크
        if (certificateDetail == null) {
            log.warn("No certificate detail found for user {} and lecture {}", userId, lectureId);
            throw new DefaultException("해당 이수증을 찾을 수 없습니다.");
        }

        // 로깅을 통한 상세 정보 확인
        log.info("Certificate Detail: title={}, teacherName={}, certificateDate={}",
                certificateDetail.getTitle(),
                certificateDetail.getTeacherName(),
                certificateDetail.getCertificateDate());

        return certificateDetail;
    }

    @Override
    public boolean issueCertificate(Long lectureId, Long userId) {
        return false;
    }
}
