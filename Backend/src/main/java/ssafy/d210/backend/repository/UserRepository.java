package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByEmail(String email);
}
