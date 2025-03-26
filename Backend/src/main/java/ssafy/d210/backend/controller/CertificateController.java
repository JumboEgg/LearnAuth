package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.service.CertificateService;

@RestController
@RequestMapping("api/certificate")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    // 수료증 조회 @GetMapping

    // 수료증 자세히 보기 @GetMapping("/detail")

    // 수료증 발급 요청 @PatchMapping("/lecture/{lectureId}/certification")
}
