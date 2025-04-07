package ssafy.d210.backend.redis;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Target;

@Component
@RequiredArgsConstructor
public class AopForTransaction {

    /**
     * 별도의 트랜잭션으로 메서드를 실행합니다.
     * Propagation.REQUIRES_NEW를 사용하여 기존 트랜잭션과 분리합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

}
