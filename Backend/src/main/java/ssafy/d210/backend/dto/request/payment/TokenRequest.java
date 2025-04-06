package ssafy.d210.backend.dto.request.payment;

import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
public class TokenRequest {
    long userId;
    BigInteger quantity;
}
