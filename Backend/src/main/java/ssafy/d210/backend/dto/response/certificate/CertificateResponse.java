package ssafy.d210.backend.dto.response.certificate;

import lombok.*;

//
@Getter
@Builder
public class CertificateResponse {
    private long lectureId;
    private String title;
    private String categoryName;
    private Integer certificate;
    private String certificateDate;
}
