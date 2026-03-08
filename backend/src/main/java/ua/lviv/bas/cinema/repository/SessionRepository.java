package ua.lviv.bas.cinema.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, JpaSpecificationExecutor<Session> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE s.id = :id")
	Optional<Session> findByIdWithLock(@Param("id") Long id);

	@Query(value = """
			SELECT
			    s.id,
			    s.start_time as startTime,
			    (s.start_time + (m.duration_minutes * INTERVAL '1 minute')) as endTime,
			    s.base_price as basePrice,
			    s.status,
			    m.id as movieId,
			    m.title as movieTitle,
			    m.duration_minutes as movieDuration,
			    h.id as hallId,
			    h.name as hallName,
			    (SELECT COUNT(seat.id) FROM seats seat WHERE seat.hall_id = h.id) as hallCapacity,
			    (SELECT COUNT(b.id) FROM bookings b WHERE b.session_id = s.id AND b.status = 'CONFIRMED') as ticketsSold,
			    COALESCE((SELECT SUM(b.total_price) FROM bookings b WHERE b.session_id = s.id AND b.status = 'CONFIRMED'), 0) as totalRevenue
			FROM sessions s
			JOIN movies m ON m.id = s.movie_id
			JOIN cinema_halls h ON h.id = s.hall_id
			WHERE s.id = :id
			""", nativeQuery = true)
	Optional<SessionAdminProjection> findAdminProjectionById(@Param("id") Long id);

	@Query(value = """
			SELECT
			    s.id,
			    s.start_time as startTime,
			    (s.start_time + (m.duration_minutes * INTERVAL '1 minute')) as endTime,
			    s.base_price as basePrice,
			    s.status,
			    m.id as movieId,
			    m.title as movieTitle,
			    m.duration_minutes as movieDuration,
			    h.id as hallId,
			    h.name as hallName,
			    (SELECT COUNT(seat.id) FROM seats seat WHERE seat.hall_id = h.id) as hallCapacity,
			    (SELECT COUNT(b.id) FROM bookings b WHERE b.session_id = s.id AND b.status = 'CONFIRMED') as ticketsSold,
			    COALESCE((SELECT SUM(b.total_price) FROM bookings b WHERE b.session_id = s.id AND b.status = 'CONFIRMED'), 0) as totalRevenue
			FROM sessions s
			JOIN movies m ON m.id = s.movie_id
			JOIN cinema_halls h ON h.id = s.hall_id
			WHERE (:hallId IS NULL OR s.hall_id = :hallId)
			  AND (:movieTitle IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :movieTitle, '%')))
			  AND (:status IS NULL OR s.status = :status)
			  AND (cast(:dateFrom as date) IS NULL OR s.start_time >= cast(:dateFrom as timestamp))
			  AND (cast(:dateTo as date) IS NULL OR s.start_time <= cast(:dateTo as timestamp) + interval '1 day')
			ORDER BY
			    CASE s.status
			        WHEN 'SCHEDULED' THEN 1
			        WHEN 'ONGOING' THEN 2
			        WHEN 'CANCELLED' THEN 3
			        WHEN 'COMPLETED' THEN 4
			        ELSE 5
			    END,
			    s.start_time DESC
			""", countQuery = """
			SELECT COUNT(s.id)
			FROM sessions s
			JOIN movies m ON m.id = s.movie_id
			WHERE (:hallId IS NULL OR s.hall_id = :hallId)
			  AND (:movieTitle IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :movieTitle, '%')))
			  AND (:status IS NULL OR s.status = :status)
			  AND (cast(:dateFrom as date) IS NULL OR s.start_time >= cast(:dateFrom as timestamp))
			  AND (cast(:dateTo as date) IS NULL OR s.start_time <= cast(:dateTo as timestamp) + interval '1 day')
			""", nativeQuery = true)
	Page<SessionAdminProjection> findAdminSessionsNative(@Param("hallId") Long hallId,
			@Param("movieTitle") String movieTitle, @Param("status") String status,
			@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, Pageable pageable);

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
			JOIN cinema_halls h ON h.id = s.hall_id
			WHERE s.status = 'ONGOING'
			AND (s.start_time + (m.duration_minutes * INTERVAL '1 minute')) <= :currentTime
			""", nativeQuery = true)
	List<Session> findSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);

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

	@Query("SELECT s.id FROM Session s WHERE s.status = 'SCHEDULED' AND s.startTime > :currentTime")
	Page<Long> findScheduledSessionIds(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);
}