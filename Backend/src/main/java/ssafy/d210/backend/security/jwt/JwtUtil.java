package ssafy.d210.backend.security.jwt;
//
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${JWT_SECRET}") String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }

        log.info("JWT Secret initialized");
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    /**
     * JWT 토큰에서 이메일 추출
     */
    public String getEmail(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * JWT 토큰에서 카테고리 추출
     */
    public String getCategory(String token) {
        Claims claims = parseToken(token);
        return claims.get("category", String.class);
    }

    /**
     * JWT 토큰 만료 여부 확인
     */
    public Boolean isExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }

    /**
     * JWT 토큰 생성
     */
    public String createJwt(String category, String email, Long expiredMS, Long userId) {
        return Jwts.builder()
                .claim("category", category)
                .claim("email", email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMS))
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰 파싱 및 검증
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("JWT 파싱 오류", e);
            throw e;
        }
    }
}