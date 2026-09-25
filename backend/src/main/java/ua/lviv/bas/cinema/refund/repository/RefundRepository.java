package ua.lviv.bas.cinema.refund.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.projection.StuckRefundProjection;
import ua.lviv.bas.cinema.refund.repository.projection.TicketRefundCandidateProjection;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long>, JpaSpecificationExecutor<Refund> {

    @EntityGraph(attributePaths = {"user", "ticket", "payment", "payment.booking", "payment.booking.session",
            "payment.booking.session.movie"})
    @Override
    Page<Refund> findAll(Specification<Refund> spec, Pageable pageable);

    List<Refund> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = :status")
    BigDecimal sumAmountByPaymentIdAndStatus(@Param("paymentId") Long paymentId, @Param("status") RefundStatus status);

    @Query("SELECT COUNT(r) > 0 FROM Refund r JOIN r.items i WHERE i.ticket.id = :ticketId AND r.status = :status")
    boolean existsByItemsTicketIdAndStatus(@Param("ticketId") Long ticketId, @Param("status") RefundStatus status);

    @Query("SELECT r.id as refundId, i.ticket.id as ticketId, r.payment.liqpayOrderId as liqpayOrderId, "
            + "r.payment.id as paymentId, r.totalAmount as refundAmount, r.payment.amount as paymentAmount, "
            + "r.createdDate as createdDate FROM Refund r JOIN r.items i "
            + "WHERE r.status = :status AND r.createdDate < :cutoff ORDER BY r.createdDate")
    List<StuckRefundProjection> findStuckRefunds(@Param("status") RefundStatus status,
                                                 @Param("cutoff") Instant cutoff);

    @Query("SELECT t.id as ticketId, t.user.id as userId FROM Ticket t WHERE t.status = :ticketStatus "
            + "AND t.booking.session.status = :sessionStatus AND NOT EXISTS (SELECT 1 FROM Refund r JOIN r.items i "
            + "WHERE i.ticket = t AND r.status IN :blockingRefundStatuses)")
    List<TicketRefundCandidateProjection> findRefundCandidates(@Param("ticketStatus") TicketStatus ticketStatus,
            @Param("sessionStatus") CinemaSessionStatus sessionStatus,
            @Param("blockingRefundStatuses") List<RefundStatus> blockingRefundStatuses);
}
