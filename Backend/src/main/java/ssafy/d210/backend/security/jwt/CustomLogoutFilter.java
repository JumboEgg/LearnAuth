package ssafy.d210.backend.security.jwt;
//
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import ssafy.d210.backend.security.repository.TokenRepository;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class CustomLogoutFilter extends GenericFilter {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void doFilter(ServletRequest request, ServletResponse reesponse, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) reesponse, chain);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException{

        String requestUri = request.getRequestURI();
        if (!requestUri.endsWith("/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refresh = request.getHeader("refresh");

        if (refresh == null) {
            log.error("Refresh 토큰이 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            log.error("Refresh 토큰이 만료되었습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            log.error("Refresh 토큰이 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 토큰 검증 부분 수정
        String redisKey = "refresh:" + refresh;
        boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));

        if (!hasKey) {
            log.error("Refresh 토큰이 DB에 없습니다.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // Redis에서 직접 삭제
        redisTemplate.delete(redisKey);

        response.setHeader("refresh", null);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
