package ssafy.d210.backend.service;

import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.report.ReportRequest;
import ssafy.d210.backend.dto.response.report.ReportResponse;

import java.util.List;

@Service
public interface ReportService {

    // 사용자 신고 내역 조회
    public List<ReportResponse> getReports(Long userId);

    // 신고 자세히 보기
    public ReportResponse getReportDetail(Long reportId);

    // 신고 등록
    public ReportResponse createReport(ReportRequest request);
}
