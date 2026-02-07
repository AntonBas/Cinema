package ua.lviv.bas.cinema.repository.projection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;

@Repository
public interface SessionAdminProjectionRepository
		extends JpaRepository<SessionAdminProjection, Long>, JpaSpecificationExecutor<SessionAdminProjection> {

	@Query("""
			SELECT s FROM SessionAdminProjection s
			WHERE (:status IS NULL OR s.status = :status)
			AND (:hallId IS NULL OR s.hallId = :hallId)
			AND (:movieId IS NULL OR s.movieId = :movieId)
			AND (:dateFrom IS NULL OR DATE(s.startTime) >= :dateFrom)
			AND (:dateTo IS NULL OR DATE(s.startTime) <= :dateTo)
			""")
	Page<SessionAdminProjection> findWithFilters(@Param("status") String status, @Param("hallId") Long hallId,
			@Param("movieId") Long movieId, @Param("dateFrom") java.time.LocalDate dateFrom,
			@Param("dateTo") java.time.LocalDate dateTo, Pageable pageable);
}