package ssafy.D210.lecture_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lecture")
public class LectureController {

    @GetMapping
    public String lecture() {
        return "lecture";
    }
}
