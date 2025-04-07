package ssafy.d210.backend.dto.response.certificate;

import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;

@Getter
@Builder
public class CertificateToken {

    private BigInteger token;
}
