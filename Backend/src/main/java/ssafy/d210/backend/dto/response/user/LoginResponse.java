package ssafy.d210.backend.dto.response.user;

import lombok.Builder;
import lombok.Getter;
//
@Getter
@Builder
public class LoginResponse {
    // 이 부분이 한나가 쓴 부분
    private long userId;
    private String nickname;
    private int certificateCount;
    private String wallet;
}