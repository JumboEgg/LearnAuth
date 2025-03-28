package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.response.email.EmailResponse;
import ssafy.d210.backend.service.EmailService;

@RestController
@RequestMapping("/api/identities")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    @GetMapping
    public ResponseEntity<ResponseSuccessDto<EmailResponse>> identityEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(emailService.identityEmail(email));
    }
}
