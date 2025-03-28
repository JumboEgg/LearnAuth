package ssafy.d210.backend.dto.response.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    // TODO : 해명해주세요:( 어디다 사용하는지 알려주세요
    private String nickname;
    private String message;
}
