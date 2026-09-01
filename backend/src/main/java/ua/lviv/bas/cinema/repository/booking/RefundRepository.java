package ua.lviv.bas.cinema.repository.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.status.RefundStatus;
import ua.lviv.bas.cinema.repository.booking.projection.StuckRefundProjection;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query("SELECT COUNT(r) > 0 FROM Refund r JOIN r.items i WHERE i.ticket.id = :ticketId AND r.status = :status")
    boolean existsByItemsTicketIdAndStatus(@Param("ticketId") Long ticketId, @Param("status") RefundStatus status);

    @Query("SELECT r.id as refundId, i.ticket.id as ticketId FROM Refund r JOIN r.items i "
            + "WHERE r.status = :status AND r.createdDate < :cutoff")
    List<StuckRefundProjection> findStuckRefunds(@Param("status") RefundStatus status,
                                                 @Param("cutoff") LocalDateTime cutoff);
}