package ua.lviv.bas.cinema.repository.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByUniqueCode(String uniqueCode);

    Optional<Ticket> findByIdAndUserIdAndStatus(Long ticketId, Long userId, TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.status = :status AND t.booking.session.status = :sessionStatus")
    List<Ticket> findActiveTicketsBySessionStatus(@Param("status") TicketStatus status,
                                                  @Param("sessionStatus") CinemaSessionStatus sessionStatus);

    @Query("SELECT t FROM Ticket t WHERE t.booking.session.startTime BETWEEN :fromTime AND :toTime AND t.status = :status")
    List<Ticket> findByBookingSessionStartTimeBetweenAndStatus(@Param("fromTime") LocalDateTime fromTime,
                                                               @Param("toTime") LocalDateTime toTime, @Param("status") TicketStatus status);

    @Query("SELECT t FROM Ticket t WHERE t.status IN :statuses AND t.purchaseTime < :time")
    List<Ticket> findByStatusInAndPurchaseTimeBefore(@Param("statuses") List<TicketStatus> statuses,
                                                     @Param("time") LocalDateTime time);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status AND CAST(t.purchaseTime AS date) = CURRENT_DATE")
    long countByStatusToday(@Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE CAST(t.purchaseTime AS date) = CURRENT_DATE")
    long countToday();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status IN :statuses AND t.booking.session.startTime > :currentTime")
    long countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(@Param("ticketTypeId") Long ticketTypeId,
                                                                       @Param("statuses") List<TicketStatus> statuses, @Param("currentTime") LocalDateTime currentTime);
}