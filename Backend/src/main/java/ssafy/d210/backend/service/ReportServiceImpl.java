package ssafy.d210.backend.service;
//
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
import ssafy.d210.backend.exception.service.LectureNotFoundException;
import ssafy.d210.backend.redis.DistributedLock;
import ssafy.d210.backend.repository.ReportRepository;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService{

    // report data db 조회, 저장 위한 repository
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
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
        List<Report> reports = findReports(userId);

        System.out.println("조회된 신고 개수 = " + reports.size());
        // 클라이언트에 반환할 DTO 리스트 만드는 변수
        List<ReportResponse> responseList = reports.stream()
                .map(report -> new ReportResponse(
                        report.getId(),
                        report.getUserLecture().getLecture().getTitle(),
                        report.getReportType()))
                .collect(Collectors.toList());

        return responseUtil.successResponse(responseList, HereStatus.SUCCESS_REPORT_LIST);
    }

    @Transactional(readOnly = true)
    protected List<Report> findReports(Long userId) {
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new EntityIsNullException("유효한 userId가 아닙니다.");
        }
        return reportRepository.findByUserLectureUserId(userId);
    }

    @Override
    @Transactional
    public ResponseSuccessDto<ReportDetailResponse> getReportDetail(Long reportId) {
        // reportId로 DB에서 report entity 찾기
        Report report = findReport(reportId);

        // 왜 신고 내역 없는지 확인
        if (report.getUserLecture().getLecture() == null) {
            throw new LectureNotFoundException("해당 신고에 연결된 강의 정보가 없습니다.");
        }
        // 강의 제목 가져오기 : report -> userLecture -> lecture -> title
        String title = report.getUserLecture().getLecture().getTitle();
        // 강의 제목, report entity에서 ReportType, ReportContent 바로 가져오기
        ReportDetailResponse response = new ReportDetailResponse(
                title,
                report.getReportType(),
                report.getReportContent()
        );
        return responseUtil.successResponse(response, HereStatus.SUCCESS_REPORT_DETAIL);
    }

    @Override
    @DistributedLock(key = "#createReport")
    public ResponseSuccessDto<Void> createReport(ReportRequest request, Long userId) {

        // JWTToken으로 사용자 정보 가져오기 : 수정 필요할 수도
        UserLecture userLecture = findUserLecture(userId, request.getLectureId());

        // 새 report entity 생성
        Report report = new Report();
        report.setReportType(request.getReportType());
        report.setReportContent(request.getReportContent());
        Report savedReport = reportRepository.save(report);

        // UserLecture와 report 연관관계 설정
        userLecture.setReport(savedReport);
        userLectureRepository.save(userLecture);

        return responseUtil.successResponse(null, HereStatus.SUCCESS_REPORT_CREATED);
    }

    @Transactional(readOnly = true)
    protected UserLecture findUserLecture(Long userId, Long lectureId) {
        return userLectureRepository
                .findByUserIdAndLectureId(userId, lectureId)
                .orElseThrow(() -> new EntityIsNullException("해당 유저와 강의에 연결된 UserLecture가 없습니다."));
    }

    @Transactional(readOnly = true)
    protected Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityIsNullException("해당 신고 id가 없습니다."));
    }
}
