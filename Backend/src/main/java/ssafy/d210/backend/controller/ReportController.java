package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.service.ReportService;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 신고 하기 @PostMapping

    // 신고 내역 전체 조회 @GetMapping

    // 신고 자세히 보기 @GetMapping("/{reportId}")


}
