package ssafy.d210.backend.dto.response.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDetailResponse {
    private String title;
    private int reportType;
    private String reportDetail;
}
