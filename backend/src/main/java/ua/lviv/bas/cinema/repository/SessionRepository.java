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
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long>, JpaSpecificationExecutor<Session> {

	@EntityGraph(attributePaths = { "movie", "hall", "seatReservations" })
	@Override
	Optional<Session> findById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Session s WHERE s.id = :id")
	Optional<Session> findByIdWithLock(@Param("id") Long id);

	@Query("SELECT s FROM Session s WHERE s.status = 'SCHEDULED' AND s.startTime <= :currentTime")
	List<Session> findSessionsToStart(@Param("currentTime") LocalDateTime currentTime);

	@Query("""
			SELECT s FROM Session s
			WHERE s.status = 'ONGOING'
			AND FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) <= :currentTime
			""")
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

	@Query("""
			SELECT
			    s.id as id,
			    s.startTime as startTime,
			    FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			    s.basePrice as basePrice,
			    s.status as status,
			    s.movie.id as movieId,
			    s.movie.title as movieTitle,
			    s.movie.durationMinutes as movieDuration,
			    s.hall.id as hallId,
			    s.hall.name as hallName,
			    (SELECT COUNT(se) FROM Seat se WHERE se.hall.id = s.hall.id AND se.active = true) as hallCapacity,
			    (SELECT COUNT(t) FROM Ticket t WHERE t.booking.session.id = s.id AND t.booking.status = 'CONFIRMED') as ticketsSold,
			    (SELECT COALESCE(SUM(t.finalPrice), 0) FROM Ticket t WHERE t.booking.session.id = s.id AND t.booking.status = 'CONFIRMED') as totalRevenue
			FROM Session s
			""")
	Page<SessionAdminProjection> findAdminSessions(Specification<Session> spec, Pageable pageable);

	@Query("""
			SELECT
			    s.id as id,
			    s.startTime as startTime,
			    FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			    s.basePrice as basePrice,
			    s.status as status,
			    s.movie.id as movieId,
			    s.movie.title as movieTitle,
			    s.movie.posterFileName as moviePosterFileName,
			    s.movie.ageRating as movieAgeRating,
			    s.movie.durationMinutes as movieDuration,
			    s.hall.id as hallId,
			    s.hall.name as hallName,
			    (SELECT COUNT(se) FROM Seat se WHERE se.hall.id = s.hall.id AND se.active = true) as hallCapacity,
			    (SELECT COUNT(se) FROM Seat se WHERE se.hall.id = s.hall.id AND se.active = true) -
			    (SELECT COUNT(bs) FROM SeatReservation bs WHERE bs.session.id = s.id AND bs.status IN ('CONFIRMED', 'ACTIVE', 'PAID')) as availableSeats
			FROM Session s
			WHERE s.status = 'SCHEDULED'
			AND s.startTime > CURRENT_TIMESTAMP
			""")
	Page<SessionScheduleProjection> findScheduleSessions(Specification<Session> spec, Pageable pageable);

	@Query("""
			SELECT
			    s.id as id,
			    s.startTime as startTime,
			    FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			    s.basePrice as basePrice,
			    s.status as status,
			    s.movie.id as movieId,
			    s.movie.title as movieTitle,
			    s.movie.posterFileName as moviePosterFileName,
			    s.movie.ageRating as movieAgeRating,
			    s.movie.durationMinutes as movieDuration,
			    s.hall.id as hallId,
			    s.hall.name as hallName,
			    (SELECT COUNT(se) FROM Seat se WHERE se.hall.id = s.hall.id AND se.active = true) as hallCapacity,
			    (SELECT COUNT(se) FROM Seat se WHERE se.hall.id = s.hall.id AND se.active = true) -
			    (SELECT COUNT(bs) FROM SeatReservation bs WHERE bs.session.id = s.id AND bs.status IN ('CONFIRMED', 'ACTIVE', 'PAID')) as availableSeats
			FROM Session s
			WHERE s.id = :id
			""")
	Optional<SessionScheduleProjection> findScheduleProjectionById(@Param("id") Long id);

	@Query("""
			SELECT
			    s.id as id,
			    s.startTime as startTime,
			    FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			    s.basePrice as basePrice,
			    s.status as status,
			    s.movie.id as movieId,
			    s.movie.title as movieTitle,
			    s.movie.durationMinutes as movieDuration,
			    s.hall.id as hallId,
			    s.hall.name as hallName,
			    (SELECT COUNT(se) FROM Seat se WHERE se.hall.id = s.hall.id AND se.active = true) as hallCapacity,
			    (SELECT COUNT(t) FROM Ticket t WHERE t.booking.session.id = s.id AND t.booking.status = 'CONFIRMED') as ticketsSold,
			    (SELECT COALESCE(SUM(t.finalPrice), 0) FROM Ticket t WHERE t.booking.session.id = s.id AND t.booking.status = 'CONFIRMED') as totalRevenue
			FROM Session s
			WHERE s.id = :id
			""")
	Optional<SessionAdminProjection> findAdminProjectionById(@Param("id") Long id);
}