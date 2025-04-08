package ssafy.d210.backend.dto.response.certificate;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

//
@Getter
@Builder
public class CertificateResponse {
    private long lectureId;
    private String title;
    private String categoryName;
    private Integer certificate;
    // userlecture entity    private LocalDate certificateDate; 에 따른 데이터 형식 수정
    private Date certificateDate;

    // native query 사용시 필요
    public CertificateResponse(long lectureId, String title, String categoryName, Integer certificate, Date certificateDate) {
        this.lectureId = lectureId;
        this.title = title;
        this.categoryName = categoryName;
        this.certificate = certificate;
        this.certificateDate = certificateDate;
    }
}
