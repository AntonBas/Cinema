package ua.lviv.bas.cinema.repository;

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

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

	Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	Page<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, BookingStatus status, Pageable pageable);

	Optional<Booking> findByIdAndUserId(Long id, Long userId);

	List<Booking> findByExpiresAtBeforeAndStatus(LocalDateTime expiresAt, BookingStatus status);

	@Query("SELECT b FROM Booking b WHERE b.expiresAt BETWEEN :start AND :end AND b.status = :status")
	List<Booking> findByExpiresAtBetweenAndStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("status") BookingStatus status);

	@Modifying
	@Query("DELETE FROM Booking b WHERE b.status IN :statuses AND b.createdAt < :cutoffDate")
	int deleteByStatusInAndCreatedAtBefore(@Param("statuses") List<BookingStatus> statuses,
			@Param("cutoffDate") LocalDateTime cutoffDate);
}