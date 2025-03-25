package ssafy.d210.backend.dto.response.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportResponse {
    private Long reportId;
    private String title;
    private int reportType;
}
