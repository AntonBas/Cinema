package ua.lviv.bas.cinema.repository.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.audit.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);

	Page<AuditLog> findByChangedByOrderByChangedAtDesc(String changedBy, Pageable pageable);

	@Query("SELECT a FROM AuditLog a WHERE a.changedAt BETWEEN :from AND :to")
	List<AuditLog> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}