package ssafy.d210.backend.dto.request.report;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class ReportRequest {
    private Long userId;
    private Long lectureId;
    private int reportType;
    private String reportContent;
}
