package ua.lviv.bas.cinema.repository.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.audit.AuditLogDetail;

@Repository
public interface AuditLogDetailRepository extends JpaRepository<AuditLogDetail, Long> {
}