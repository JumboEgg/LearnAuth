package ssafy.d210.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Configuration
@EnableRedisRepositories
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
            log.info("Redisson Client 성공적으로 만들어 졌다.");
            return client;
        } catch (Exception e) {
            log.error("Redisson Client를 만드는게 실패했다.", e);
            throw new RuntimeException("Redis 연결 실패", e);
        }
    }
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
        if (password!=null && !password.isEmpty()) {
            redisConfig.setPassword(password);
        }
        return new LettuceConnectionFactory(redisConfig);
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();

        // 기본 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

        // 커스텀 ZonedDateTime 처리를 위한 모듈 추가
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
            @Override
            public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateStr = p.getText();

                // 여러 파싱 메서드를 시도
                List<Supplier<ZonedDateTime>> parsingMethods = new ArrayList<>();

                // 1. 기본 ISO 파싱
                parsingMethods.add(() -> ZonedDateTime.parse(dateStr));

                // 2. ISO_OFFSET_DATE_TIME 형식
                parsingMethods.add(() -> ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

                // 3. ISO_ZONED_DATE_TIME 형식
                parsingMethods.add(() -> ZonedDateTime.parse(dateStr, DateTimeFormatter.ISO_ZONED_DATE_TIME));

                // 4. 커스텀 포맷
                parsingMethods.add(() -> ZonedDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")));

                // 5. 문제의 날짜 형식 교정 (yyyy-mm-dd 형식을 yyyy-MM-dd로)
                parsingMethods.add(() -> {
                    if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                        String[] parts = dateStr.split("T");
                        if (parts.length > 0) {
                            String[] dateParts = parts[0].split("-");
                            if (dateParts.length == 3) {
                                int month = Integer.parseInt(dateParts[1]);
                                // 월이 12보다 크면 보정
                                String correctedMonth = month <= 12 ? dateParts[1] : String.format("%02d", (month % 12 == 0 ? 12 : month % 12));
                                String correctedDate = dateParts[0] + "-" + correctedMonth + "-" + dateParts[2];
                                String correctedDateStr = correctedDate + (parts.length > 1 ? "T" + parts[1] : "");
                                return ZonedDateTime.parse(correctedDateStr);
                            }
                        }
                    }
                    throw new DateTimeException("Format not recognized for correction: " + dateStr);
                });

                // 모든 메서드 시도
                for (Supplier<ZonedDateTime> method : parsingMethods) {
                    try {
                        return method.get();
                    } catch (Exception e) {

                    }
                }

                // 모든 방법 실패 시
                throw new IOException("Failed to parse ZonedDateTime: " + dateStr);
            }
        });

        module.addSerializer(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
            @Override
            public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                // ISO 8601 표준 형식으로 직렬화
                gen.writeString(value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }
        });

        objectMapper.registerModule(module);

        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(10L))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

}
