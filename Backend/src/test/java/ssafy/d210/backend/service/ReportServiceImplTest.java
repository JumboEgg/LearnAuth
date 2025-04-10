package ssafy.d210.backend.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
//import ssafy.d210.backend.dto.common.ResponseSuccessDto;
//import ssafy.d210.backend.dto.response.report.ReportDetailResponse;
//import ssafy.d210.backend.dto.response.report.ReportResponse;
//import ssafy.d210.backend.entity.Lecture;
//import ssafy.d210.backend.entity.Report;
//import ssafy.d210.backend.enumeration.response.HereStatus;
//import ssafy.d210.backend.repository.PaymentRatioRepository;
//import ssafy.d210.backend.repository.ReportRepository;
//import ssafy.d210.backend.repository.UserLectureRepository;
//import ssafy.d210.backend.repository.UserRepository;
//import ssafy.d210.backend.util.ResponseUtil;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {
//
//    @Mock private ReportRepository reportRepository;
//    @Mock private UserRepository userRepository;
//    @Mock private PaymentRatioRepository paymentRatioRepository;
//    @Mock private UserLectureRepository userLectureRepository;
//    @Mock private ResponseUtil responseUtil;
//
//    @InjectMocks
//    ReportServiceImpl reportService;
//
//    @Test
//    @DisplayName("신고 리스트 조회 성공")
//    void getReports_success() {
//        Long userId = 1L;
//
//        // given
//        Report report = new Report();
//        Lecture lecture = new Lecture();
//        lecture.setTitle("수학강의");
//        report.setLecture(lecture);
//        report.setId(10L);
//        report.setReportType(1);
//
//        when(reportRepository.findReportsByUserId(userId)).thenReturn(List.of(report));
//
//        ReportResponse expectedResponse = new ReportResponse(10L, "수학강의", 1);
//
//        ResponseSuccessDto<List<ReportResponse>> mockResponse = ResponseSuccessDto.<List<ReportResponse>>builder()
//                .timestamp(ZonedDateTime.now())
//                .code(200)
//                .status(HereStatus.SUCCESS_REPORT_LIST.name())
//                .data(List.of(expectedResponse))
//                .build();
//
//        when(responseUtil.successResponse(any(), eq(HereStatus.SUCCESS_REPORT_LIST)))
//                .thenReturn(mockResponse);
//
//        // when
//        ResponseSuccessDto<List<ReportResponse>> result = reportService.getReports(userId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_REPORT_LIST.name());
//        assertThat(result.getData().get(0).getTitle()).isEqualTo("수학강의");
//
//    }
//
//    @Test
//    @DisplayName("신고 상세 조회 성공")
//    void getReportDetail_success() {
//        Long reportId = 100L;
//
//        // given
//        Lecture lecture = new Lecture();
//        lecture.setTitle("테스트강의");
//
//        Report report = new Report();
//        report.setLecture(lecture);
//        report.setReportType(1);
//        report.setReportContent("신고합니다.");
//
//        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
//
//        ReportDetailResponse detailResponse = new ReportDetailResponse("테스트강의", 1, "신고합니다.");
//
//        ResponseSuccessDto<ReportDetailResponse> mockResponse = ResponseSuccessDto.<ReportDetailResponse>builder()
//                    .code(200)
//                    .status(HereStatus.SUCCESS_REPORT_DETAIL.name())
//                    .data(detailResponse)
//                    .build();
//
//        when(responseUtil.successResponse(any(), eq(HereStatus.SUCCESS_REPORT_DETAIL)))
//                .thenReturn(mockResponse);
//
//        // when
//        ResponseSuccessDto<ReportDetailResponse> result = reportService.getReportDetail(reportId);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getStatus()).isEqualTo(HereStatus.SUCCESS_REPORT_DETAIL.name());
//        assertThat(result.getData().getTitle()).isEqualTo("테스트강의");
//        assertThat(result.getData().getReportDetail()).isEqualTo("신고합니다.");
//
//    }
//
//
}