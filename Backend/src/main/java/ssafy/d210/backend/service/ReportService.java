package ssafy.d210.backend.service;
//
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.report.ReportRequest;
import ssafy.d210.backend.dto.response.report.ReportDetailResponse;
import ssafy.d210.backend.dto.response.report.ReportResponse;

import java.util.List;


public interface ReportService {

    // 사용자 신고 내역 조회
    public List<ReportResponse> getReports(Long userId);

    // 받은 신고 자세히 보기
    public ReportDetailResponse getReportDetail(Long reportId);

    // 신고 등록
    public ReportResponse createReport(ReportRequest request, Long userId);
}
