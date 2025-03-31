package ssafy.d210.backend.dto.request.transaction;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class MetaTxRequest {
    private String from;
    private String to;
    private BigInteger gas;
    private BigInteger deadline;
    private byte[] data;
}
