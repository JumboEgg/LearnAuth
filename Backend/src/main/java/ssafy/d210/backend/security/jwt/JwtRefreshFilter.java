package ssafy.d210.backend.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.security.repository.TokenRepository;
import ssafy.d210.backend.util.ResponseUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class JwtRefreshFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ResponseUtil responseUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 리프레시 토큰 엔드포인트인 경우에만 처리
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (!requestURI.endsWith("/api/auth/refresh") || !requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 응답 헤더 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 쿠키에서 리프레시 토큰 추출
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }

        if (refresh == null) {
            log.error("Refresh token이 없습니다.");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Refresh token이 없습니다.");
            return;
        }

        try {
            // 토큰 검증
            if (jwtUtil.isExpired(refresh)) {
                log.error("Refresh token이 만료되었습니다.");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Refresh token이 만료되었습니다.");
                return;
            }

            String category = jwtUtil.getCategory(refresh);
            if (!category.equals("refresh")) {
                log.error("유효하지 않은 토큰 타입입니다.");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰 타입입니다.");
                return;
            }

            // DB에 저장된 토큰인지 확인
            Boolean isExist = tokenRepository.existsByRefresh(refresh);
            if (!isExist) {
                log.error("토큰이 DB에 저장되어 있지 않습니다.");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "토큰이 DB에 저장되어 있지 않습니다.");
                return;
            }

            // 사용자 정보 추출
            String email = jwtUtil.getEmail(refresh);
            Long userId = jwtUtil.getUserId(refresh);

            // 사용자 존재 확인
            User user = userRepository.findUserByEmail(email);
            if (user == null) {
                log.error("사용자가 존재하지 않습니다.");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "사용자가 존재하지 않습니다.");
                return;
            }

            // 새 액세스 토큰 생성
            String newAccessToken = jwtUtil.createJwt("access", email, 3 * 6 * 600000L, userId);

            // 응답 생성
            response.setHeader("access", newAccessToken);
            response.setStatus(HttpStatus.OK.value());

            ResponseSuccessDto<Boolean> res = responseUtil.successResponse(true, HereStatus.SUCCESS_REFRESH);

            String jsonResponse = objectMapper.writeValueAsString(res);
            PrintWriter writer = response.getWriter();
            writer.write(jsonResponse);
            writer.flush();

            log.info("액세스 토큰 갱신 성공: {}", email);

        } catch (ExpiredJwtException e) {
            log.error("Refresh token이 만료되었습니다.");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Refresh token이 만료되었습니다.");
        } catch (Exception e) {
            log.error("Refresh token 에러", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        String jsonResponse = String.format("{\"error\":\"토큰 갱신 실패\", \"message\":\"%s\"}", message);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}