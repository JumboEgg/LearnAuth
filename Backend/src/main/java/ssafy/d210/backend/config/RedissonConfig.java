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

    private static final String REDDISON_HOST_PREFIX= "redis://";

    @Bean
    @Profile({"local"})
    public RedissonClient redissonClient() {

        Config config = new Config();
        config.useSingleServer().setAddress(REDDISON_HOST_PREFIX + host + ":" + port);

        return Redisson.create(config);
    }

    @Bean
    @Profile({"dev"})
    public RedissonClient redissonClientDev() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(REDDISON_HOST_PREFIX + host +":" + port)
                .setPassword(password)
                .setConnectTimeout(10000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        return Redisson.create(config);
    }

}
