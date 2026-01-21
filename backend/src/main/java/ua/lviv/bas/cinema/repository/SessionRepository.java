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

	@Query("SELECT s FROM Session s WHERE s.status = :scheduledStatus AND s.startTime <= :currentTime")
	List<Session> findSessionsToStart(@Param("currentTime") LocalDateTime currentTime,
			@Param("scheduledStatus") CinemaSessionStatus scheduledStatus);

	@Query(value = """
			SELECT s.* FROM sessions s
			JOIN movies m ON s.movie_id = m.id
			WHERE s.status = 'ONGOING'
			AND (s.start_time + (m.duration_minutes || ' minutes')::interval) <= :currentTime
			""", nativeQuery = true)
	List<Session> findSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall ORDER BY s.startTime DESC")
	Page<Session> findAllWithMovieAndHall(Pageable pageable);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie m LEFT JOIN FETCH s.hall h WHERE "
			+ "(:search IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
			+ "(:adminView = true OR s.status = :scheduledStatus) " + "ORDER BY s.startTime DESC")
	Page<Session> findByMovieTitleWithMovieAndHall(@Param("search") String search,
			@Param("adminView") boolean adminView, @Param("scheduledStatus") CinemaSessionStatus scheduledStatus,
			Pageable pageable);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE "
			+ "s.startTime >= :start AND s.startTime <= :end AND "
			+ "(:adminView = true OR s.status = :scheduledStatus) " + "ORDER BY s.startTime DESC")
	Page<Session> findByStartTimeBetweenWithMovieAndHall(@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end, @Param("adminView") boolean adminView,
			@Param("scheduledStatus") CinemaSessionStatus scheduledStatus, Pageable pageable);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE " + "s.hall.id = :hallId AND "
			+ "(:adminView = true OR s.status = :scheduledStatus) " + "ORDER BY s.startTime DESC")
	Page<Session> findByHallIdWithMovieAndHall(@Param("hallId") Long hallId, @Param("adminView") boolean adminView,
			@Param("scheduledStatus") CinemaSessionStatus scheduledStatus, Pageable pageable);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE "
			+ "s.movie.id = :movieId AND " + "(:adminView = true OR s.status = :scheduledStatus) "
			+ "ORDER BY s.startTime DESC")
	Page<Session> findByMovieIdWithMovieAndHall(@Param("movieId") Long movieId, @Param("adminView") boolean adminView,
			@Param("scheduledStatus") CinemaSessionStatus scheduledStatus, Pageable pageable);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE s.status = :status "
			+ "ORDER BY s.startTime DESC")
	Page<Session> findByStatusWithMovieAndHall(@Param("status") CinemaSessionStatus status, Pageable pageable);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE "
			+ "s.status = :scheduledStatus AND " + "s.startTime > CURRENT_TIMESTAMP " + "ORDER BY s.startTime ASC")
	Page<Session> findAvailableSessionsWithMovieAndHall(@Param("scheduledStatus") CinemaSessionStatus scheduledStatus,
			Pageable pageable);

	@Query(value = """
			SELECT s.* FROM sessions s
			JOIN movies m ON s.movie_id = m.id
			WHERE s.hall_id = :hallId
			AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId)
			AND (
			    (s.start_time < :endTime)
			    AND
			    (s.start_time + (m.duration_minutes || ' minutes')::interval) > :startTime
			)
			""", nativeQuery = true)
	List<Session> findConflictingSessions(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);

	@Query(value = """
			SELECT COUNT(s) > 0 FROM sessions s
			JOIN movies m ON s.movie_id = m.id
			WHERE s.hall_id = :hallId
			AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId)
			AND (
			    (s.start_time < :endTime)
			    AND
			    (s.start_time + (m.duration_minutes || ' minutes')::interval) > :startTime
			)
			""", nativeQuery = true)
	boolean existsConflictingSession(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);
}