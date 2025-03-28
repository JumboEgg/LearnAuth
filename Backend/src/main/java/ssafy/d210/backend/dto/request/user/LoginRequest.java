package ssafy.d210.backend.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
//
@Getter
public class LoginRequest {
    private String email;
    private String password;
}
