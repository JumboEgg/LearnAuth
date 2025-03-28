package ssafy.d210.backend.dto.response.report;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class ReportResponse {
    private Long reportId;
    private String title;
    private int reportType;

    // 생성자 추가
    public ReportResponse(Long reportId, String title, int reportType) {
        this.reportId = reportId;
        this.title = title;
        this.reportType = reportType;
    }
}
