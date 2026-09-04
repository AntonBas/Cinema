package ua.lviv.bas.cinema.refund.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.projection.StuckRefundProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = :status")
    BigDecimal sumAmountByPaymentIdAndStatus(@Param("paymentId") Long paymentId, @Param("status") RefundStatus status);

    @Query("SELECT COUNT(r) > 0 FROM Refund r JOIN r.items i WHERE i.ticket.id = :ticketId AND r.status = :status")
    boolean existsByItemsTicketIdAndStatus(@Param("ticketId") Long ticketId, @Param("status") RefundStatus status);

    @Query("SELECT r.id as refundId, i.ticket.id as ticketId, r.payment.liqpayOrderId as liqpayOrderId "
            + "FROM Refund r JOIN r.items i WHERE r.status = :status AND r.createdDate < :cutoff")
    List<StuckRefundProjection> findStuckRefunds(@Param("status") RefundStatus status,
                                                 @Param("cutoff") LocalDateTime cutoff);
}