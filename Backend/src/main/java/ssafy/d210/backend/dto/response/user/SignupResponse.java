package ssafy.d210.backend.dto.response.user;

import lombok.Builder;
import lombok.Getter;
//
@Getter
@Builder
public class SignupResponse {
    private String nickname;
    private String message;
}
