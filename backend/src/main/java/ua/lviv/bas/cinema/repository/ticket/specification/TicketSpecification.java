package ua.lviv.bas.cinema.repository.ticket.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TicketSpecification {

    public Specification<Ticket> buildForUser(Long userId, TicketStatus status, String movieTitle) {
        Specification<Ticket> filterSpec = Specification.allOf(
                filterByUserId(userId),
                filterByStatus(status),
                filterByMovieTitle(movieTitle)
        );

        Specification<Ticket> sortSpec = (root, query, cb) -> {
            if (query != null) {
                List<Order> orders = new ArrayList<>();
                orders.add(cb.asc(cb.selectCase()
                        .when(cb.equal(root.get("status"), TicketStatus.ACTIVE), 0)
                        .otherwise(1)));
                var booking = root.join("booking", JoinType.LEFT);
                var session = booking.join("session", JoinType.LEFT);
                orders.add(cb.asc(session.get("startTime")));
                query.orderBy(orders);
            }
            return cb.conjunction();
        };

        return filterSpec.and(sortSpec);
    }

    public Specification<Ticket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public Specification<Ticket> hasSessionStatus(CinemaSessionStatus sessionStatus) {
        return (root, query, cb) -> {
            if (sessionStatus == null) return null;
            var booking = root.join("booking", JoinType.LEFT);
            var session = booking.join("session", JoinType.LEFT);
            return cb.equal(session.get("status"), sessionStatus);
        };
    }

    public Specification<Ticket> purchaseTimeBefore(LocalDateTime time) {
        return (root, query, cb) -> cb.lessThan(root.get("purchaseTime"), time);
    }

    private Specification<Ticket> filterByUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    private Specification<Ticket> filterByStatus(TicketStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    private Specification<Ticket> filterByMovieTitle(String movieTitle) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(movieTitle)) {
                return null;
            }
            var booking = root.join("booking", JoinType.INNER);
            var session = booking.join("session", JoinType.INNER);
            var movie = session.join("movie", JoinType.INNER);
            return cb.like(cb.lower(movie.get("title")), "%" + movieTitle.toLowerCase() + "%");
        };
    }

    public Specification<Ticket> hasTicketTypeId(Long ticketTypeId) {
        return (root, query, cb) -> {
            if (ticketTypeId == null) return null;
            return cb.equal(root.get("ticketType").get("id"), ticketTypeId);
        };
    }

    public Specification<Ticket> sessionStartTimeAfter(LocalDateTime time) {
        return (root, query, cb) -> {
            var booking = root.join("booking", JoinType.LEFT);
            var session = booking.join("session", JoinType.LEFT);
            return cb.greaterThan(session.get("startTime"), time);
        };
    }
}