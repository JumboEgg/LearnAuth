package ssafy.d210.backend.security.repository;
//
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ssafy.d210.backend.security.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
    boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}
