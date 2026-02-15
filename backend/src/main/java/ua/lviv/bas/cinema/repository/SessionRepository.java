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

	@EntityGraph(attributePaths = { "movie", "hall" })
	@Override
	Optional<Session> findById(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@EntityGraph(attributePaths = { "movie", "hall" })
	@Query("SELECT s FROM Session s WHERE s.id = :id")
	Optional<Session> findByIdWithLock(@Param("id") Long id);

	@Query("""
			SELECT s.id as id, s.startTime as startTime,
			       FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			       s.basePrice as basePrice, s.status as status,
			       m.id as movieId, m.title as movieTitle, m.durationMinutes as movieDuration,
			       h.id as hallId, h.name as hallName,
			       (SELECT COUNT(b) FROM Booking b WHERE b.session = s AND b.status = 'CONFIRMED') as ticketsSold,
			       (SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.session = s AND b.status = 'CONFIRMED') as totalRevenue
			FROM Session s
			JOIN s.movie m
			JOIN s.hall h
			WHERE s.id = :id
			""")
	Optional<SessionAdminProjection> findAdminProjectionById(@Param("id") Long id);

	@Query("""
			SELECT s.id as id, s.startTime as startTime,
			       FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			       s.basePrice as basePrice, s.status as status,
			       m.id as movieId, m.title as movieTitle, m.posterFileName as moviePosterFileName,
			       m.ageRating as movieAgeRating, m.durationMinutes as movieDuration,
			       h.id as hallId, h.name as hallName,
			       (SELECT COUNT(sr) FROM SeatReservation sr WHERE sr.session = s AND sr.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')) as reservedSeats
			FROM Session s
			JOIN s.movie m
			JOIN s.hall h
			WHERE s.id = :id
			""")
	Optional<SessionScheduleProjection> findScheduleProjectionById(@Param("id") Long id);

	@Query("""
			SELECT s FROM Session s
			JOIN FETCH s.movie m
			JOIN FETCH s.hall h
			WHERE s.status = 'SCHEDULED' AND s.startTime <= :currentTime
			""")
	List<Session> findSessionsToStart(@Param("currentTime") LocalDateTime currentTime);

	@Query("""
			SELECT s FROM Session s
			JOIN FETCH s.movie m
			JOIN FETCH s.hall h
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
			SELECT s.id as id, s.startTime as startTime,
			       FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			       s.basePrice as basePrice, s.status as status,
			       m.id as movieId, m.title as movieTitle, m.posterFileName as moviePosterFileName,
			       m.ageRating as movieAgeRating, m.durationMinutes as movieDuration,
			       h.id as hallId, h.name as hallName
			FROM Session s
			JOIN s.movie m
			JOIN s.hall h
			WHERE s.status = 'SCHEDULED'
			AND s.startTime > :currentTime
			""")
	Page<SessionScheduleProjection> findAllScheduleProjections(@Param("currentTime") LocalDateTime currentTime,
			Pageable pageable);

	@Query("""
			SELECT s.id as id, s.startTime as startTime,
			       FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) as endTime,
			       s.basePrice as basePrice, s.status as status,
			       m.id as movieId, m.title as movieTitle, m.durationMinutes as movieDuration,
			       h.id as hallId, h.name as hallName,
			       (SELECT COUNT(b) FROM Booking b WHERE b.session = s AND b.status = 'CONFIRMED') as ticketsSold,
			       (SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.session = s AND b.status = 'CONFIRMED') as totalRevenue
			FROM Session s
			JOIN s.movie m
			JOIN s.hall h
			""")
	Page<SessionAdminProjection> findAllAdminProjections(Specification<Session> spec, Pageable pageable);
}