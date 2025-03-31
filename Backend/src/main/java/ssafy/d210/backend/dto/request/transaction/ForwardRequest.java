package ssafy.d210.backend.dto.request.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigInteger;

// 포워더 컨트랙트 요청 객체
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForwardRequest {
    public String from;
    public String to;
    public BigInteger gas;
    public BigInteger nonce;
    public BigInteger deadline;
    public String data;
}
