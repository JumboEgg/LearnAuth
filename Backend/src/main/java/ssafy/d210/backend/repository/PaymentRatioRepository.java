package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.PaymentRatio;

public interface PaymentRatioRepository extends JpaRepository<PaymentRatio, Long> {
}
