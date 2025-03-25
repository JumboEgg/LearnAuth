package ssafy.d210.backend.dto.response.certificate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateDetailResponse {
    private String name;
    private String title;
    private String teacherName;
    private int certificateDate;
    // TODO : qrcode 어떻게 작동 되는건지 알려주세요
    private String qrCode;
}
