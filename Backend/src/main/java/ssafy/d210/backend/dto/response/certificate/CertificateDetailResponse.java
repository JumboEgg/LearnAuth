package ssafy.d210.backend.dto.response.certificate;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class CertificateDetailResponse {
//    private String name;
    private String title;
    private String teacherName;
    private String teacherWallet;
    private int certificateDate;
    private int certificate;
    private String qrCode;
}
