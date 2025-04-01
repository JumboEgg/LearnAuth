package ssafy.d210.backend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    public String host;

    @Value("${spring.data.redis.port}")
    public int port;

    @Value("${spring.redis.password:}")  // 비밀번호가 없으면 빈 문자열
    private String password;

    private static final String REDISSON_HOST_PREFIX= "redis://";

    @Bean
    @Profile({"local"})
    public RedissonClient redissonClient() {

        Config config = new Config();
        config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + host + ":" + port);

        return Redisson.create(config);
    }

    @Bean
    @Profile({"dev"})
    public RedissonClient redissonClientDev() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDISSON_HOST_PREFIX + host +":" + port)
                .setPassword(password)
                .setConnectTimeout(5000)  // 연결 타임아웃 추가
                .setRetryAttempts(3)      // 재시도 횟수 추가
                .setRetryInterval(1000);  // 재시도 간격 추가

        return Redisson.create(config);
    }

}
