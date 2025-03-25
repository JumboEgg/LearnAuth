package ssafy.d210.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.d210.backend.service.LectureService;

@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    //전체 강의 조회
//    @GetMapping

    //추천 강의 조회
//    @GetMapping("/recommendation")

    //강의 상세 조회 @GetMapping("/{lectureId})

    //강의 검색 @GetMapping("/search")

}
