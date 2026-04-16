package ua.lviv.bas.cinema.repository.cinema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionScheduleProjection;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, JpaSpecificationExecutor<Session> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE s.id = :id")
	Optional<Session> findByIdWithLock(@Param("id") Long id);

	@Query("""
			SELECT s FROM Session s
			JOIN FETCH s.movie
			JOIN FETCH s.hall
			WHERE s.status = 'SCHEDULED' AND s.startTime <= :currentTime
			""")
	List<Session> findSessionsToStart(@Param("currentTime") LocalDateTime currentTime);

	@Query(value = """
			SELECT s.* FROM sessions s
			JOIN movies m ON m.id = s.movie_id
			WHERE s.status = 'ONGOING'
			AND (s.start_time + (m.duration_minutes * INTERVAL '1 minute')) <= :currentTime
			""", nativeQuery = true)
	List<Session> findSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);

	@Query("SELECT COUNT(s) FROM Session s WHERE s.movie.id = :movieId")
	long countByMovieId(@Param("movieId") Long movieId);

	@Query("""
			SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END
			FROM Session s
			WHERE s.hall.id = :hallId
			AND (:excludeSessionId IS NULL OR s.id != :excludeSessionId)
			AND s.status IN ('SCHEDULED', 'ONGOING')
			AND s.startTime < :endTime
			AND FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) > :startTime
			""")
	boolean existsConflictingSession(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);

	@Query(value = """
			SELECT
			    s.id as sessionId,
			    (SELECT COUNT(seat.id) FROM seats seat WHERE seat.hall_id = h.id) - COALESCE(reserved.reserved, 0) as availableSeats
			FROM sessions s
			JOIN cinema_halls h ON h.id = s.hall_id
			LEFT JOIN (
			    SELECT sr.session_id as sessionId, COUNT(sr.id) as reserved
			    FROM seat_reservations sr
			    WHERE sr.session_id IN (:sessionIds)
			    AND sr.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
			    GROUP BY sr.session_id
			) reserved ON reserved.sessionId = s.id
			WHERE s.id IN (:sessionIds)
			""", nativeQuery = true)
	List<Object[]> findAvailableSeatsBatch(@Param("sessionIds") List<Long> sessionIds);

	@Query(value = """
			SELECT
			    s.id,
			    s.start_time as startTime,
			    s.base_price as basePrice,
			    m.id as movieId,
			    m.title as movieTitle,
			    m.poster_file_name as moviePosterFileName,
			    m.age_rating as movieAgeRating,
			    m.duration_minutes as movieDuration,
			    h.id as hallId,
			    h.name as hallName,
			    (SELECT COUNT(seat.id) FROM seats seat WHERE seat.hall_id = h.id) as hallCapacity
			FROM sessions s
			JOIN movies m ON m.id = s.movie_id
			JOIN cinema_halls h ON h.id = s.hall_id
			WHERE s.id = :id
			""", nativeQuery = true)
	Optional<SessionScheduleProjection> findScheduleProjectionById(@Param("id") Long id);

	@Query(value = """
			SELECT
			    s.id,
			    s.start_time as startTime,
			    s.base_price as basePrice,
			    s.status,
			    m.id as movieId,
			    m.title as movieTitle,
			    m.duration_minutes as movieDuration,
			    h.id as hallId,
			    h.name as hallName,
			    (SELECT COUNT(seat.id) FROM seats seat WHERE seat.hall_id = h.id) as hallCapacity,
			    COALESCE((
			        SELECT COUNT(b.id) FROM bookings b
			        WHERE b.session_id = s.id AND b.status = 'CONFIRMED'
			    ), 0) as ticketsSold,
			    COALESCE((
			        SELECT SUM(b.total_price) FROM bookings b
			        WHERE b.session_id = s.id AND b.status = 'CONFIRMED'
			    ), 0) as totalRevenue
			FROM sessions s
			JOIN movies m ON m.id = s.movie_id
			JOIN cinema_halls h ON h.id = s.hall_id
			WHERE s.id IN (:ids)
			""", nativeQuery = true)
	List<SessionAdminProjection> findAdminProjectionsByIds(@Param("ids") List<Long> ids);
}