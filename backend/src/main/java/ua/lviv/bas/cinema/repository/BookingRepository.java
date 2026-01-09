package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, BookingStatus status);

	@Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status IN (:statuses) ORDER BY b.createdAt DESC")
	List<Booking> findByUserIdAndStatusIn(@Param("userId") Long userId,
			@Param("statuses") List<BookingStatus> statuses);

	List<Booking> findBySessionId(Long sessionId);

	List<Booking> findBySessionIdAndStatus(Long sessionId, BookingStatus status);

	List<Booking> findByExpiresAtBeforeAndStatus(LocalDateTime expiresAt, BookingStatus status);

	Optional<Booking> findByIdAndUserId(Long id, Long userId);

	Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

	boolean existsByUserIdAndSessionIdAndStatusIn(Long userId, Long sessionId, List<BookingStatus> statuses);

	long countBySessionIdAndStatusIn(Long sessionId, List<BookingStatus> statuses);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM Booking b WHERE b.id = :id")
	Optional<Booking> findByIdWithLock(@Param("id") Long id);

	@Query("SELECT b FROM Booking b LEFT JOIN FETCH b.payment WHERE b.id = :id")
	Optional<Booking> findByIdWithPayment(@Param("id") Long id);

	@Query("SELECT b FROM Booking b LEFT JOIN FETCH b.session s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.hall WHERE b.id = :id")
	Optional<Booking> findByIdWithDetails(@Param("id") Long id);

	@Query("SELECT b FROM Booking b WHERE b.expiresAt BETWEEN :start AND :end AND b.status = :status")
	List<Booking> findByExpiresAtBetweenAndStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("status") BookingStatus status);

	@Query("SELECT b FROM Booking b WHERE b.createdAt < :cutoffDate AND b.status IN :statuses")
	List<Booking> findOldBookingsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate,
			@Param("statuses") List<BookingStatus> statuses);

	@Modifying
	@Query("DELETE FROM Booking b WHERE b.status IN :statuses AND b.createdAt < :cutoffDate")
	int deleteByStatusInAndCreatedAtBefore(@Param("statuses") List<BookingStatus> statuses,
			@Param("cutoffDate") LocalDateTime cutoffDate);

	@Query("SELECT COUNT(b) FROM Booking b WHERE b.session.id = :sessionId AND b.status = :status")
	long countActiveBookingsForSession(@Param("sessionId") Long sessionId, @Param("status") BookingStatus status);

	@Query("SELECT b FROM Booking b WHERE b.bookingNumber = :bookingNumber")
	Optional<Booking> findByBookingNumber(@Param("bookingNumber") String bookingNumber);

	@Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.session.id = :sessionId AND b.status IN :statuses")
	List<Booking> findUserActiveBookingsForSession(@Param("userId") Long userId, @Param("sessionId") Long sessionId,
			@Param("statuses") List<BookingStatus> statuses);

	default List<Booking> findOldBookingsForCleanup(LocalDateTime cutoffDate) {
		return findOldBookingsForCleanup(cutoffDate, Arrays.asList(BookingStatus.EXPIRED, BookingStatus.CANCELLED));
	}

	default boolean hasUserActiveBookingForSession(Long userId, Long sessionId) {
		List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);
		return !findUserActiveBookingsForSession(userId, sessionId, activeStatuses).isEmpty();
	}
}