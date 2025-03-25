package ssafy.d210.backend.dto.response.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private long userId;
    private String nickname;
    private int certificateCount;
    private String wallet;
}