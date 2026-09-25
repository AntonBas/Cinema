package ua.lviv.bas.cinema.cinema.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.repository.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.cinema.repository.projection.SessionScheduleProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
public interface SessionRepository extends JpaRepository<Session, Long>, JpaSpecificationExecutor<Session> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE s.id = :id")
    Optional<Session> findByIdWithLock(@Param("id") Long id);

    Optional<Session> findByPublicId(UUID publicId);

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

    @Modifying
    @Query("UPDATE Session s SET s.status = :newStatus WHERE s.id IN :ids AND s.status = :fromStatus")
    int updateStatusForIds(@Param("ids") List<Long> ids, @Param("fromStatus") CinemaSessionStatus fromStatus,
                           @Param("newStatus") CinemaSessionStatus newStatus);

    @Query("""
            SELECT DISTINCT CAST(s.startTime AS LocalDate) AS sessionDate
            FROM Session s
            WHERE s.status = 'SCHEDULED'
              AND s.startTime > :now
              AND (:movieId IS NULL OR s.movie.id = :movieId)
            ORDER BY sessionDate
            """)
    List<LocalDate> findScheduleDates(@Param("now") LocalDateTime now, @Param("movieId") Long movieId);

    boolean existsByHallIdAndStartTimeAfterAndStatusNot(Long hallId, LocalDateTime time,
                                                        CinemaSessionStatus status);

    @Query("SELECT MIN(s.startTime) FROM Session s WHERE s.movie.id = :movieId AND s.status IN ('SCHEDULED', 'ONGOING')")
    LocalDateTime findFirstActiveSessionStart(@Param("movieId") Long movieId);

    @Query("SELECT MAX(s.startTime) FROM Session s WHERE s.movie.id = :movieId AND s.status IN ('SCHEDULED', 'ONGOING')")
    LocalDateTime findLastActiveSessionStart(@Param("movieId") Long movieId);

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
                s.id,
                s.public_id as publicId,
                s.start_time as startTime,
                s.base_price as basePrice,
                m.id as movieId,
                m.title as movieTitle,
                m.slug as movieSlug,
                m.poster_file_name as moviePosterFileName,
                m.age_rating as movieAgeRating,
                m.duration_minutes as movieDuration,
                h.id as hallId,
                h.name as hallName,
                COALESCE(sc.seat_count, 0) as hallCapacity
            FROM sessions s
            JOIN movies m ON m.id = s.movie_id
            JOIN cinema_halls h ON h.id = s.hall_id
            LEFT JOIN (
                SELECT hall_id, COUNT(*) as seat_count
                FROM seats
                WHERE hall_id IN (SELECT hall_id FROM sessions WHERE id IN (:ids))
                GROUP BY hall_id
            ) sc ON sc.hall_id = h.id
            WHERE s.id IN (:ids)
            """, nativeQuery = true)
    List<SessionScheduleProjection> findScheduleProjectionsByIds(@Param("ids") List<Long> ids);

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
                COALESCE(sc.seat_count, 0) as hallCapacity,
                COALESCE(bs.tickets_sold, 0) as ticketsSold,
                COALESCE(bs.total_revenue, 0) as totalRevenue
            FROM sessions s
            JOIN movies m ON m.id = s.movie_id
            JOIN cinema_halls h ON h.id = s.hall_id
            LEFT JOIN (
                SELECT hall_id, COUNT(*) as seat_count
                FROM seats
                WHERE hall_id IN (SELECT hall_id FROM sessions WHERE id IN (:ids))
                GROUP BY hall_id
            ) sc ON sc.hall_id = h.id
            LEFT JOIN (
                SELECT b.session_id,
                       CAST(SUM(bt.sold) AS BIGINT) as tickets_sold,
                       SUM(b.final_price) - COALESCE(SUM(bt.refunded), 0) as total_revenue
                FROM bookings b
                JOIN (
                    SELECT t.booking_id,
                           COUNT(t.id) FILTER (WHERE t.status <> 'REFUNDED') as sold,
                           SUM(r.total_amount) as refunded
                    FROM tickets t
                    LEFT JOIN refunds r ON r.ticket_id = t.id AND r.status = 'PROCESSED'
                    GROUP BY t.booking_id
                ) bt ON bt.booking_id = b.id
                WHERE b.session_id IN (:ids)
                GROUP BY b.session_id
            ) bs ON bs.session_id = s.id
            WHERE s.id IN (:ids)
            """, nativeQuery = true)
    List<SessionAdminProjection> findAdminProjectionsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.session.id = :sessionId")
    boolean hasBookings(@Param("sessionId") Long sessionId);

    @Query("SELECT COUNT(sr) > 0 FROM SeatReservation sr WHERE sr.session.id = :sessionId")
    boolean hasSeatReservations(@Param("sessionId") Long sessionId);
}
