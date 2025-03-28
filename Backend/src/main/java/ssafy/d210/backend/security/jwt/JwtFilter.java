package ssafy.d210.backend.security.jwt;
//
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.security.dto.CustomUserDetails;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 로그인 경로는 JWT 검증 건너뛰기
        if (path.contains("/api/auth/login") || path.contains("/api/auth/signup")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = request.getHeader("access");

        // 토큰 없으면 다음 필터로
        if (!StringUtils.hasText(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 만료 확인
            if (jwtUtil.isExpired(accessToken)) {
                handleExpiredToken(response);
                return;
            }

            // 토큰 카테고리 확인
            String category = jwtUtil.getCategory(accessToken);
            if (!"access".equals(category)) {
                handleInvalidToken(response, "Invalid token category");
                return;
            }

            // 토큰에서 사용자 정보 추출
            String email = jwtUtil.getEmail(accessToken);
            Long userId = jwtUtil.getUserId(accessToken);

            // 인증 객체 생성
            Authentication authentication = createAuthentication(email, userId);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 인증 성공: {}", email);

        } catch (ExpiredJwtException e) {
            handleExpiredToken(response);
            return;
        } catch (JwtException e) {
            handleInvalidToken(response, e.getMessage());
            return;
        } catch (Exception e) {
            log.error("JWT 처리 중 오류 발생", e);
            handleInvalidToken(response, "Internal server error");
            return;
        }

        filterChain.doFilter(request, response);
    }
    private Authentication createAuthentication(String email, Long userId) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(""); // 빈 비밀번호 - 실제 인증에는 사용되지 않음

        CustomUserDetails userDetails = new CustomUserDetails(user, userId);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    private void handleExpiredToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401, \"error\":\"Unauthorized\", \"message\":\"Access token expired\"}");
    }

    private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401, \"error\":\"Unauthorized\", \"message\":\"" + message + "\"}");
    }
}
