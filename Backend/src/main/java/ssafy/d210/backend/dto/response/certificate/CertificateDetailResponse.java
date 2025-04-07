package ssafy.d210.backend.dto.response.certificate;

import lombok.*;

import java.math.BigInteger;
import java.sql.Date;

//
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDetailResponse {
    private String title;
    private String teacherName;
    private String teacherWallet;
    private Date certificateDate;
    private Integer certificate;
}
