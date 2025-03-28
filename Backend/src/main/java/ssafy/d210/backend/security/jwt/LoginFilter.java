package ssafy.d210.backend.security.jwt;
//
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;
import ssafy.d210.backend.dto.common.ResponseSuccessDto;
import ssafy.d210.backend.dto.request.user.LoginRequest;
import ssafy.d210.backend.dto.response.user.LoginResponse;
import ssafy.d210.backend.entity.User;
import ssafy.d210.backend.entity.UserLecture;
import ssafy.d210.backend.enumeration.response.HereStatus;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.security.entity.Token;
import ssafy.d210.backend.security.repository.TokenRepository;
import ssafy.d210.backend.service.UserLectureService;
import ssafy.d210.backend.util.ResponseUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;
    private final ResponseUtil responseUtil;
    private final UserLectureService userLectureService;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       UserRepository userRepository, TokenRepository tokenRepository,
                       ResponseUtil responseUtil, UserLectureService userLectureService) {
        super(new AntPathRequestMatcher("/api/auth/login"));
        this.userLectureService = userLectureService;
        setAuthenticationManager(authenticationManager);

        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.objectMapper = new ObjectMapper();
        this.responseUtil = responseUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        log.info("Login attempt");

        LoginRequest loginRequest;

        try {
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            log.info("Request Body: {}", messageBody);
            loginRequest = objectMapper.readValue(messageBody, LoginRequest.class);
        } catch (IOException e) {
            log.error("Failed to parse request body", e);
            throw new BadCredentialsException("Invalid request format");
        }

        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            throw new BadCredentialsException("Email and password are required");
        }

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        log.info("Login attempt with email: {}", email);

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(email, password, Collections.emptyList());

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        log.info("Login successful for user: {}", authResult.getName());

        String email = authResult.getName();
        User user = userRepository.findUserByEmail(email);

        if (user == null) {
            log.error("User not found with email: {}", email);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        Long userId = user.getId();

        log.info("User email: {}", email);
        log.info("User ID: {}", userId);

        // 토큰 생성 (유효시간 계산 수정)
        String access = jwtUtil.createJwt("access", email, 3 * 6 * 600000L, userId);
        String refresh = jwtUtil.createJwt("refresh", email, 4 * 6 * 600000L, userId);

        // 리프레시 토큰 저장
        addRefresh(email, refresh, 86400000L);

        // 응답 설정
        response.setHeader("access", access);
        response.setHeader("refresh", refresh);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

//        LoginResponse loginResponse = LoginResponse.builder()
//                .userId(userId)
//                .nickname(user.getNickname())
//                .certificateCount(0)
////                .certificateCount(user.getCertificateCount() != null ? user.getCertificateCount() : 0)
//                .wallet(user.getWallet() != null ? user.getWallet() : "")
//                .build();
//
//        ResponseSuccessDto<LoginResponse> res = responseUtil.successResponse(loginResponse, HereStatus.SUCCESS_LOGIN);
//
//        // ObjectMapper를 사용하여 JSON 변환
//        String jsonResponse = objectMapper.writeValueAsString(res);
//
//        response.getWriter().write(jsonResponse);
//        response.getWriter().flush();
        // Status 정보
        int code = HttpStatus.OK.value();
        String status = HttpStatus.OK.name();

        // 사용자 정보
        String nickname = user.getNickname() != null ? user.getNickname() : "";

        List<UserLecture> userLectureList = userLectureService.findAllByUserId(user.getId());
        log.info("userLectureList {}", userLectureList.size());

        String wallet = user.getWallet() != null ? user.getWallet() : "";

        // 간단한 JSON 문자열 생성 (포맷 문자열 사용 대신 문자열 연결)
        String jsonResponse =
                "{" +
                        "\"timeStamp\":\"" + Instant.now().toString() + "\"," +
                        "\"code\":" + code + "," +
                        "\"status\":\"" + status + "\"," +
                            "\"data\":{" +
                            "\"userId\":" + userId + "," +
                            "\"nickname\":\"" + nickname + "\"," +
                            "\"certificateCount\":" + userLectureList.size() + "," +
                            "\"wallet\":\"" + wallet + "\"" +
                            "}" +
                        "}";

        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();

        log.info("로그인 성공");

        // 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    private void addRefresh(String email, String refreshToken, long expiredMs) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiredMs);

        Token token = new Token();
        token.setEmail(email);
        token.setRefresh(refreshToken);
        token.setExpiration(expirationDate.toString());

        tokenRepository.save(token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {
        log.info("로그인 실패: {}", failed.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorMessage = failed.getMessage() != null ? failed.getMessage() : "Invalid credentials";
        String jsonResponse = String.format("{\"error\":\"인증 실패\", \"message\":\"%s\"}", errorMessage);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}