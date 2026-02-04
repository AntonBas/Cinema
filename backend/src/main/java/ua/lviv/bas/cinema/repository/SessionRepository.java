package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ua.lviv.bas.cinema.domain.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, JpaSpecificationExecutor<Session> {

	@EntityGraph(attributePaths = { "movie", "hall", "hall.seats", "bookedSeats" })
	@Override
	Page<Session> findAll(Specification<Session> spec, Pageable pageable);

	@EntityGraph(attributePaths = { "movie", "hall", "hall.seats", "bookedSeats" })
	@Override
	Optional<Session> findById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Session s WHERE s.id = :id")
	Optional<Session> findByIdWithLock(@Param("id") Long id);

	@Query("SELECT s FROM Session s WHERE s.status = 'SCHEDULED' AND s.startTime <= :currentTime")
	List<Session> findSessionsToStart(@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT s FROM Session s WHERE s.status = 'ONGOING' "
			+ "AND s.startTime + s.movie.durationMinutes * FUNCTION('NUMTODSINTERVAL', 1, 'MINUTE') <= :currentTime")
	List<Session> findSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " + "FROM Session s " + "JOIN s.movie m "
			+ "WHERE s.hall.id = :hallId " + "AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId) "
			+ "AND s.status IN ('SCHEDULED', 'ONGOING') " + "AND ((s.startTime < :endTime) "
			+ "AND (s.startTime + m.durationMinutes * FUNCTION('NUMTODSINTERVAL', 1, 'MINUTE') > :startTime))")
	boolean existsConflictingSession(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);
}