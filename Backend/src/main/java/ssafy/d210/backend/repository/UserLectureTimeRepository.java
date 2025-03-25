package ssafy.d210.backend.repository;

import lombok.extern.java.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.UserLectureTime;

public interface UserLectureTimeRepository extends JpaRepository<UserLectureTime, Long> {
}
