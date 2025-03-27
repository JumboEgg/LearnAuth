package ssafy.d210.backend.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import ssafy.d210.backend.repository.UserLectureRepository;
import ssafy.d210.backend.repository.UserRepository;
import ssafy.d210.backend.security.jwt.CustomLogoutFilter;
import ssafy.d210.backend.security.jwt.JwtFilter;
import ssafy.d210.backend.security.jwt.JwtUtil;
import ssafy.d210.backend.security.jwt.LoginFilter;
import ssafy.d210.backend.security.repository.TokenRepository;
import ssafy.d210.backend.service.UserLectureService;
import ssafy.d210.backend.util.ResponseUtil;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final ResponseUtil responseUtil;
    private final UserLectureService userLectureService;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화
        http.csrf(csrf -> csrf.disable());

        // 폼 로그인 비활성화
        http.formLogin(form -> form.disable());

        // HTTP Basic 인증 비활성화
        http.httpBasic(basic -> basic.disable());

        // 세션 상태 없음 설정
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 요청 권한 설정
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/").permitAll()
                        .requestMatchers("/reissue").permitAll()
                        .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
                        .requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
        );

        // 필터 설정 - 순서 변경
        AuthenticationManager authManager = authenticationManager();

        // LoginFilter 먼저 추가
        LoginFilter loginFilter = new LoginFilter(authManager, jwtUtil, userRepository, tokenRepository, responseUtil, userLectureService);
        http.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // 그 후 JwtFilter 추가
        http.addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 로그아웃 필터 추가
        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, tokenRepository), LogoutFilter.class);

        return http.build();
    }
}