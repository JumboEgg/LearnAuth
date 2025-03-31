package ssafy.d210.backend.dto.response.certificate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
//
@Getter
@Builder
public class CertificateDetailResponse {
//    private String name;
    private String title;
    private String teacherName;
    private String teacherWallet;
    private Integer certificateDate;
    private Integer certificate;
    private String qrCode;
}
