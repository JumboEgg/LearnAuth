package ssafy.d210.backend.dto.response.certificate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateResponse {
    private long lectureId;
    private String title;
    private String categoryName;
    private int certificate;
}
