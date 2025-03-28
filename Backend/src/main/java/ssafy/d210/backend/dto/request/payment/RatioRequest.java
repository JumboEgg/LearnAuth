package ssafy.d210.backend.dto.request.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatioRequest {
    private String email;
    private int ratio;
    private boolean lecturer;
}
