package ssafy.d210.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.parameters.P;

@Configuration
@Slf4j
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    public String host;

    @Value("${spring.data.redis.port}")
    public int port;

    @Value("${spring.data.redis.password:}")  // 비밀번호가 없으면 빈 문자열
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
        try {
            log.info("Redis Connection Details - Host: {}, Port: {}", host, port);

            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://" + host + ":" + port)
                    .setPassword(password)
                    .setConnectTimeout(5000)
                    .setRetryAttempts(3)
                    .setRetryInterval(1000);

            RedissonClient client = Redisson.create(config);
            log.info("Redisson Client created successfully");
            return client;
        } catch (Exception e) {
            log.error("Failed to create Redisson Client", e);
            throw new RuntimeException("Redis connection failed", e);
        }
    }

}
