package ssafy.d210.backend.dto.request.certificate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateRequest {
    Long userId;
    String cid;
}
