package ssafy.d210.backend.repository;
//
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.User;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByEmail(String email);
    // 강의 등록 시 orElseThrow 사용 위해 Optional 사용
    Optional<User> findOptionalUserByEmail(String email);
    boolean existsByEmail(String email);

    // 이메일 닉네임 중복 확인
//    boolean existsByEmailOrNickname(String email, String nickname);
    boolean existsByNickname(String nickname);
    // 아니.. 이거.. 명명규칙 잘 모르겠어서 지피티 추천명으로 갑니다...
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
