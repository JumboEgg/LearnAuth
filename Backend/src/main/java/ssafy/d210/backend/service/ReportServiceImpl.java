package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.report.ReportRequest;
import ssafy.d210.backend.dto.response.report.ReportDetailResponse;
import ssafy.d210.backend.dto.response.report.ReportResponse;
import ssafy.d210.backend.entity.Report;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.exception.service.EntityIsNullException;
import ssafy.d210.backend.redis.DistributedLock;
import ssafy.d210.backend.repository.PaymentRatioRepository;
import ssafy.d210.backend.repository.ReportRepository;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService{

    // report data db 조회, 저장 위한 repository
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PaymentRatioRepository paymentRatioRepository;
    // 현재 로그인한 사용자의 UserLecture를 조회하기 위한 Repository : 구현 필요
    // JWT -> 현재 사용자 ID : 그 사용자 ID로 UserLecture 조회 "UserLectureRepository"
    // Long currentUserId = jwtUtil.getCurrentUserId(); -> 이런 식으로 id 추출
    private final UserLectureRepository userLectureRepository;
    private final ResponseUtil responseUtil;

    @Override
    @Transactional
    public ResponseSuccessDto<List<ReportResponse>> getReports(Long userId) {
        // ReportRequest, 200ok
        // userId를 이용해 UserLecture와 연관된 Report들 조회 (ReportRepository에 커스텀 메서드 추가)
        List<Report> reports = findReportsByUserId(userId);


        List<ReportResponse> responseList = reports.stream()
                .map(report -> new ReportResponse(
                        report.getId(),
                        report.getLecture().getTitle(),
                        report.getReportType()
                ))
                .collect(Collectors.toList());


        return responseUtil.successResponse(responseList, HereStatus.SUCCESS_REPORT_LIST);
    }

    @Transactional(readOnly = true)
    protected List<Report> findReportsByUserId(Long userId) {
        return reportRepository.findReportsByUserId(userId);
    }


    @Override
    @Transactional
    public ResponseSuccessDto<ReportDetailResponse> getReportDetail(Long reportId) {
        // reportId로 DB에서 report entity 찾기
        Report report = findReport(reportId);

        // 강의 제목, report entity에서 ReportType, ReportContent 바로 가져오기
        ReportDetailResponse response = new ReportDetailResponse(
                report.getLecture().getTitle(),
                report.getReportType(),
                report.getReportContent()
        );
        return responseUtil.successResponse(response, HereStatus.SUCCESS_REPORT_DETAIL);
    }

    @Override
    @DistributedLock(key = "#createReport")
    @Transactional(rollbackFor = Throwable.class)
    public ResponseSuccessDto<Void> createReport(ReportRequest request, Long userId) {

        Optional<UserLecture> userLecture = findByUserIdAndLectureId(userId, request.getLectureId());
        userLecture.get().setReport(1);
        userLectureRepository.save(userLecture.get());

        List<Report> reports = paymentRatioRepository.findPaymentRatiosByLectureId(request.getLectureId()).stream()
                .map(paymentRatio -> {
                    Report report = new Report();
                    report.setReportType(request.getReportType());
                    report.setReportContent(request.getReportContent());
                    report.setUser(paymentRatio.getUser());
                    report.setLecture(paymentRatio.getLecture());
                    return report;
                })
                .collect(Collectors.toList());

        reportRepository.saveAll(reports);

        return responseUtil.successResponse(null, HereStatus.SUCCESS_REPORT_CREATED);
    }
    @Transactional(readOnly = true)
    protected Optional<UserLecture> findByUserIdAndLectureId(Long userId, Long lectureId) {
        return userLectureRepository.findByUserIdAndLectureId(userId, lectureId);
    }

    @Transactional(readOnly = true)
    protected Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityIsNullException("해당 신고 id가 없습니다."));
    }
}
