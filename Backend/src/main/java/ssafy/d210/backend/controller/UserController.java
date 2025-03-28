package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/user")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공")
    })
    public ResponseEntity<ResponseSuccessDto<SignupResponse>> signup(@RequestBody @Valid SignupRequest userSignupRequest) {
        return ResponseEntity.ok(userService.signup(userSignupRequest));
    }


    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공")
    })
    public ResponseEntity<ResponseSuccessDto<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        // 로그인 처리
        throw new UnsupportedOperationException("LoginFilter에서 처리");
    }
}
