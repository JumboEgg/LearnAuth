package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
}
