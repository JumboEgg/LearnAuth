package ssafy.d210.backend.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotNull(message = "이메일은 필수 입력 값입니다.")
    @NotBlank(message = "이메일 값을 입력해주세요.")
    @Email(message = "이메일 형식이 아닙니다.")
    private String email;

    @NotNull(message = "비밀번호는 필수 입력 값입니다.")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotNull(message = "닉네임은 필수 입력 값입니다.")
    @NotBlank(message = "닉네임를 입력해주세요.")
    private String nickname;

//    @NotNull(message = "지갑은 필수 입력 값입니다.")
//    @NotBlank(message = "지갑을 입력해주세요.")
//    private String wallet;

    @NotNull(message = "이름은 필수 입력 값입니다.")
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

//    private String userKey;

}
