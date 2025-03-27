package ssafy.d210.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.quiz.QuizResultRequest;
import ssafy.d210.backend.dto.response.quiz.QuizResponse;
import ssafy.d210.backend.service.QuizService;
import ssafy.d210.backend.service.QuizServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/lecture/{lectureId}/quiz")
@RequiredArgsConstructor
@Tag(name = "QuizController", description = "퀴즈 조회 및 제출")
public class QuizController {

    private final QuizService quizService;

    // 퀴즈 조회 @GetMapping
    @GetMapping
    @Operation(summary = "퀴즈 조회", description = "미완임다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 조회 성공")
    })
    public ResponseEntity<ResponseSuccessDto<List<QuizResponse>>> getQuizList(
            @PathVariable Long lectureId
    ) {
        return ResponseEntity.ok(quizService.getQuizzes(lectureId));
    }

    // 퀴즈 제출 @PostMapping
    @PostMapping
    @Operation(summary = "퀴즈 제출", description = "미완임다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀴즈 제출 성공")
    })
    public ResponseEntity<ResponseSuccessDto<Object>> submitQuiz(
            @PathVariable Long lectureId,
            @RequestBody QuizResultRequest request
    ) {

        return ResponseEntity.ok(quizService.submitQuiz(lectureId, request));
    }
}
