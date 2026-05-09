package ua.lviv.bas.cinema.repository.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, Long entityId);

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:changedBy IS NULL OR a.changedBy = :changedBy)")
    Page<AuditLog> findByFilters(@Param("entityType") String entityType,
                                 @Param("action") AuditAction action,
                                 @Param("changedBy") String changedBy,
                                 Pageable pageable);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.details WHERE a.id IN :ids ORDER BY a.changedAt DESC")
    List<AuditLog> findByIdsWithDetails(@Param("ids") List<Long> ids);
}