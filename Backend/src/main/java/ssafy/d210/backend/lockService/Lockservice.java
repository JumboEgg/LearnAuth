package ssafy.d210.backend.lockService;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class Lockservice {

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("락 획득 실패: " + lockKey);
            }

            try {
                return supplier.get();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생");
        }
    }
}
