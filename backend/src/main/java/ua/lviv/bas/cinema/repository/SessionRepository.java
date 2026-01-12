package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Session s WHERE s.id = :id")
	Optional<Session> findByIdWithLock(@Param("id") Long id);

	@Query("SELECT s FROM Session s WHERE s.status = 'SCHEDULED' AND s.startTime <= :currentTime")
	List<Session> findSessionsToStart(@Param("currentTime") LocalDateTime currentTime);

	@Query("""
			    SELECT s FROM Session s
			    WHERE s.status = 'ONGOING'
			    AND (s.startTime + FUNCTION('NUMTODSINTERVAL', s.movie.durationMinutes, 'MINUTE')) <= :currentTime
			""")
	List<Session> findSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT s FROM Session s WHERE "
			+ "(:search IS NULL OR LOWER(s.movie.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
			+ "(:adminView = true OR s.status = ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.SCHEDULED)")
	Page<Session> findByMovieTitle(@Param("search") String search, @Param("adminView") boolean adminView,
			Pageable pageable);

	@Query("SELECT s FROM Session s WHERE " + "s.startTime >= :start AND s.startTime <= :end AND "
			+ "(:adminView = true OR s.status = ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.SCHEDULED)")
	Page<Session> findByStartTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("adminView") boolean adminView, Pageable pageable);

	@Query("SELECT s FROM Session s WHERE " + "s.hall.id = :hallId AND "
			+ "(:adminView = true OR s.status = ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.SCHEDULED)")
	Page<Session> findByHallId(@Param("hallId") Long hallId, @Param("adminView") boolean adminView, Pageable pageable);

	@Query("SELECT s FROM Session s WHERE " + "s.movie.id = :movieId AND "
			+ "(:adminView = true OR s.status = ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.SCHEDULED)")
	Page<Session> findByMovieId(@Param("movieId") Long movieId, @Param("adminView") boolean adminView,
			Pageable pageable);

	Page<Session> findByStatus(CinemaSessionStatus status, Pageable pageable);

	@Query("SELECT s FROM Session s WHERE "
			+ "s.status = ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.SCHEDULED AND "
			+ "s.startTime > CURRENT_TIMESTAMP")
	Page<Session> findAvailableSessions(Pageable pageable);

	@Query("""
			    SELECT s FROM Session s
			    WHERE s.hall.id = :hallId
			    AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId)
			    AND (
			        (s.startTime < :endTime)
			        AND
			        (s.startTime + FUNCTION('NUMTODSINTERVAL', s.movie.durationMinutes, 'MINUTE')) > :startTime
			    )
			""")
	List<Session> findConflictingSessions(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);

	@Query("""
			    SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Session s
			    WHERE s.hall.id = :hallId
			    AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId)
			    AND (
			        (s.startTime < :endTime)
			        AND
			        (s.startTime + FUNCTION('NUMTODSINTERVAL', s.movie.durationMinutes, 'MINUTE')) > :startTime
			    )
			""")
	boolean existsConflictingSession(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);
}