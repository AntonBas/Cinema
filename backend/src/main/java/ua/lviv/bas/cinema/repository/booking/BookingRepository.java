package ua.lviv.bas.cinema.repository.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

	Page<Booking> findByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

	Page<Booking> findByUserIdAndStatusOrderByCreatedDateDesc(Long userId, BookingStatus status, Pageable pageable);

	Optional<Booking> findByIdAndUserId(Long id, Long userId);

	List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime expiresAt);

	@Query("SELECT b FROM Booking b WHERE b.expiresAt BETWEEN :start AND :end AND b.status = :status")
	List<Booking> findByExpiresAtBetweenAndStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("status") BookingStatus status);

	@Modifying
	@Query("DELETE FROM Booking b WHERE b.status IN :statuses AND b.createdDate < :cutoffDate")
	int deleteByStatusInAndCreatedDateBefore(@Param("statuses") List<BookingStatus> statuses,
			@Param("cutoffDate") LocalDateTime cutoffDate);

	@Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
	long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status);

	@Query("SELECT b FROM Booking b JOIN FETCH b.user u JOIN FETCH b.session s JOIN FETCH s.movie WHERE b.id = :id")
	Optional<Booking> findByIdWithDetails(@Param("id") Long id);

	@Query("SELECT b FROM Booking b WHERE b.session.id = :sessionId AND b.status IN :statuses")
	List<Booking> findBySessionIdAndStatusIn(@Param("sessionId") Long sessionId,
			@Param("statuses") List<BookingStatus> statuses);
}