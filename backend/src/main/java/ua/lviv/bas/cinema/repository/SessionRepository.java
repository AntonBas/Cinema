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

	@Query(value = """
			SELECT s.* FROM sessions s
			JOIN movies m ON s.movie_id = m.id
			WHERE s.status = 'ONGOING'
			AND (s.start_time + (m.duration_minutes || ' minutes')::interval) <= :currentTime
			""", nativeQuery = true)
	List<Session> findSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT s FROM Session s WHERE "
			+ "(:search IS NULL OR LOWER(s.movie.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
			+ "(:startTime IS NULL OR s.startTime >= :startTime) AND "
			+ "(:endTime IS NULL OR s.startTime <= :endTime) AND " + "(:movieId IS NULL OR s.movie.id = :movieId) AND "
			+ "(:hallId IS NULL OR s.hall.id = :hallId) AND " + "(:status IS NULL OR s.status = :status) AND "
			+ "(:adminView = true OR s.status = ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.SCHEDULED)")
	Page<Session> findByFilters(@Param("search") String search, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("movieId") Long movieId, @Param("hallId") Long hallId,
			@Param("status") CinemaSessionStatus status, @Param("adminView") boolean adminView, Pageable pageable);

	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE "
			+ "(:search IS NULL OR LOWER(s.movie.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
			+ "(:startTime IS NULL OR s.startTime >= :startTime) AND "
			+ "(:endTime IS NULL OR s.startTime <= :endTime) AND " + "(:movieId IS NULL OR s.movie.id = :movieId) AND "
			+ "(:hallId IS NULL OR s.hall.id = :hallId) AND " + "(:status IS NULL OR s.status = :status) AND "
			+ "(:adminView = true OR s.status = ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus.SCHEDULED)")
	Page<Session> findByFiltersWithMovieAndHall(@Param("search") String search,
			@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
			@Param("movieId") Long movieId, @Param("hallId") Long hallId, @Param("status") CinemaSessionStatus status,
			@Param("adminView") boolean adminView, Pageable pageable);

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