package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.service.QuizService;

@RestController
@RequestMapping("/api/lecture/{lectureId}/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // 퀴즈 조회 @GetMapping

    // 퀴즈 제출 @PostMapping
}
