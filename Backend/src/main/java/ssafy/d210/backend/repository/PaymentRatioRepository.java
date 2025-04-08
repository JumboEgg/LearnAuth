package ssafy.d210.backend.repository;
//
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.PaymentRatio;

import java.util.List;

public interface PaymentRatioRepository extends JpaRepository<PaymentRatio, Long> {

    List<PaymentRatio> findPaymentRatiosByUserId(Long userId);

}
