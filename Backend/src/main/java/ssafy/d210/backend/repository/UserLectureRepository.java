package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.UserLecture;

public interface UserLectureRepository extends JpaRepository<UserLecture, Long> {
}
