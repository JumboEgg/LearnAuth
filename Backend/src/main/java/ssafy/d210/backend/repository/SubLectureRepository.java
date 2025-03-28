package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ssafy.d210.backend.entity.SubLecture;

import java.util.List;

public interface SubLectureRepository extends JpaRepository<SubLecture, Long> {

    List<SubLecture> findSubLectureByLectureIdOrderById(Long lectureId);
}
