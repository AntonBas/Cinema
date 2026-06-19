package ua.lviv.bas.cinema.repository.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime expiresAt);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.status IN :statuses AND b.createdDate < :cutoffDate")
    int deleteByStatusInAndCreatedDateBefore(@Param("statuses") List<BookingStatus> statuses,
                                             @Param("cutoffDate") LocalDateTime cutoffDate);
}