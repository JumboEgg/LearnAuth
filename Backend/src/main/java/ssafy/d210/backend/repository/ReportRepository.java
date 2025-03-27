package ssafy.d210.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.d210.backend.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
