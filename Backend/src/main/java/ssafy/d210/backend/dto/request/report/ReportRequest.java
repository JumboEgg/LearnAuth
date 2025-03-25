package ssafy.d210.backend.dto.request.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private long lectureId;
    private long reportType;
    private String reportContent;
}
