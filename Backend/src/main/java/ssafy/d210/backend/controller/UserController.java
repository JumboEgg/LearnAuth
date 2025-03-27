package ssafy.d210.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseSuccessDto<SignupResponse>> signup(@RequestBody @Valid SignupRequest userSignupRequest) {
        return ResponseEntity.ok(userService.signup(userSignupRequest));
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseSuccessDto<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        // 로그인 처리
        throw new UnsupportedOperationException("LoginFilter에서 처리");
    }
}
