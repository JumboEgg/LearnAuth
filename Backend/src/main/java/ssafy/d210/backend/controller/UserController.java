package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.LoginRequest;
import ssafy.d210.backend.dto.request.user.SignupRequest;
import ssafy.d210.backend.dto.response.user.LoginResponse;
import ssafy.d210.backend.dto.response.user.SignupResponse;
import ssafy.d210.backend.service.UserService;
//
@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 실명, 닉네임, 지갑을 이용해 회원가입을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공")
    })
    public ResponseEntity<ResponseSuccessDto<SignupResponse>> signup(@RequestBody @Valid SignupRequest userSignupRequest)
            throws Exception {
        return ResponseEntity.ok(userService.signup(userSignupRequest));
    }


    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 이용해 로그인을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공")
    })
    public ResponseEntity<ResponseSuccessDto<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        // 로그인 처리
        throw new UnsupportedOperationException("LoginFilter에서 처리");
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃을 합니다.", parameters = {
            @Parameter(name = "Refresh", description = "Refresh Token", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰 또는 토큰 없음")
    })
    public ResponseEntity<ResponseSuccessDto<Boolean>> logout() {
        throw new UnsupportedOperationException("CustomLogoutFilter에서 처리");
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용해 액세스 토큰을 갱신합니다.", parameters = {
            @Parameter(name = "Refresh", description = "Refresh Token", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    public ResponseEntity<ResponseSuccessDto<Boolean>> refreshToken() {
        // 실제 로직은 TokenRefreshFilter에서 처리
        return ResponseEntity.ok(new ResponseSuccessDto<>(true));
    }
}
