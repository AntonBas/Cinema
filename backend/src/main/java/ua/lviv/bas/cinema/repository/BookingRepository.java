package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

	boolean existsByUserIdAndSessionIdAndStatusIn(Long userId, Long sessionId, List<BookingStatus> statuses);

	long countBySessionIdAndStatusIn(Long sessionId, List<BookingStatus> statuses);

	@Query("SELECT b FROM Booking b LEFT JOIN FETCH b.payment WHERE b.id = :id")
	Optional<Booking> findByIdWithPayment(@Param("id") Long id);

	@Query("SELECT b FROM Booking b " + "LEFT JOIN FETCH b.session s " + "LEFT JOIN FETCH s.movie "
			+ "LEFT JOIN FETCH s.hall " + "WHERE b.id = :id")
	Optional<Booking> findByIdWithDetails(@Param("id") Long id);
}