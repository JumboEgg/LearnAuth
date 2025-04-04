package ssafy.d210.backend.security.repository;

import org.springframework.data.repository.CrudRepository;
import ssafy.d210.backend.security.entity.Token;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, String> {
    boolean existsByRefresh(String refresh);
    Optional<Token> findByRefresh(String refresh);
    void deleteByRefresh(String refresh);
}
