package gruposantoro.elyctishuella.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gruposantoro.elyctishuella.model.ScanLog;

public interface ScanLogRepository extends JpaRepository<ScanLog, Long> {
    List<ScanLog> findAllByDateBetween(LocalDateTime from, LocalDateTime to);

}
