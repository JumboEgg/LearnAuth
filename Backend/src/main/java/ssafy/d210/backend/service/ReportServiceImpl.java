package ssafy.d210.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ssafy.d210.backend.dto.request.report.ReportRequest;
import ssafy.d210.backend.dto.response.report.ReportResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{
    @Override
    public List<ReportResponse> getReports(Long userId) {
        return null;
    }

    @Override
    public ReportResponse getReportDetail(Long reportId) {
        return null;
    }

    @Override
    public ReportResponse createReport(ReportRequest request) {
        return null;
    }
}
