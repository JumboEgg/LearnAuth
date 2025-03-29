package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.email.EmailResponse;
import ssafy.d210.backend.service.EmailService;
//
@RestController
@RequestMapping("/api/identities")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    @GetMapping
    @Operation(summary = "이메일 중복 확인", description = "{email}이 user table에 저장되어 있으면 true를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 중복 확인 성공")
    })
    public ResponseEntity<ResponseSuccessDto<EmailResponse>> identityEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(emailService.identityEmail(email));
    }
}
