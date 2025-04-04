package ssafy.d210.backend.security.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
@Getter
@Setter
@RedisHash(value = "refresh", timeToLive = 14400)
public class Token {

    @Id
    private String refresh;
    private String email;

    public Token() {}
    public Token(String refresh, String email) {
        this.refresh = refresh;
        this.email = email;
    }
}
