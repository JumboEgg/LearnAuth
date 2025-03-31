package ssafy.d210.backend.dto.request.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    long userId;
    int quantity;
}
