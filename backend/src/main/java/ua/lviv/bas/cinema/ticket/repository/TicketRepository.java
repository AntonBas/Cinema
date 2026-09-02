package ua.lviv.bas.cinema.ticket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByUniqueCode(String uniqueCode);

    Optional<Ticket> findByIdAndUserIdAndStatus(Long ticketId, Long userId, TicketStatus status);

    @Override
    @EntityGraph(attributePaths = {"ticketType", "booking.session.movie", "booking.session.hall",
            "seatReservation.seat", "user"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);
}