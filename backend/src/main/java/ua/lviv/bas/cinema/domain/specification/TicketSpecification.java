package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;

@Component
public class TicketSpecification {

	public Specification<Ticket> buildForUser(Long userId, TicketFilterRequest filter) {
		return Specification.allOf(filterByUserId(userId), filterByStatus(filter.getStatus()),
				filterByMovieId(filter.getMovieId()),
				filterByPurchaseDateRange(filter.getPurchaseDateFrom(), filter.getPurchaseDateTo()),
				filterBySessionDateRange(filter.getSessionDateFrom(), filter.getSessionDateTo()));
	}

	private Specification<Ticket> filterByUserId(Long userId) {
		return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
	}

	private Specification<Ticket> filterByStatus(TicketStatus status) {
		return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
	}

	private Specification<Ticket> filterByMovieId(Long movieId) {
		return (root, query, cb) -> {
			if (movieId == null)
				return null;
			Join<Object, Object> booking = root.join("booking", JoinType.INNER);
			Join<Object, Object> session = booking.join("session", JoinType.INNER);
			return cb.equal(session.get("movie").get("id"), movieId);
		};
	}

	private Specification<Ticket> filterByPurchaseDateRange(LocalDate from, LocalDate to) {
		return (root, query, cb) -> {
			if (from == null && to == null)
				return null;

			LocalDateTime start = from != null ? from.atStartOfDay() : null;
			LocalDateTime end = to != null ? to.atTime(LocalTime.MAX) : null;

			if (start != null && end != null) {
				return cb.between(root.get("purchaseTime"), start, end);
			} else if (start != null) {
				return cb.greaterThanOrEqualTo(root.get("purchaseTime"), start);
			} else {
				return cb.lessThanOrEqualTo(root.get("purchaseTime"), end);
			}
		};
	}

	private Specification<Ticket> filterBySessionDateRange(LocalDate from, LocalDate to) {
		return (root, query, cb) -> {
			if (from == null && to == null)
				return null;

			Join<Object, Object> booking = root.join("booking", JoinType.INNER);
			Join<Object, Object> session = booking.join("session", JoinType.INNER);

			LocalDateTime start = from != null ? from.atStartOfDay() : null;
			LocalDateTime end = to != null ? to.atTime(LocalTime.MAX) : null;

			if (start != null && end != null) {
				return cb.between(session.get("startTime"), start, end);
			} else if (start != null) {
				return cb.greaterThanOrEqualTo(session.get("startTime"), start);
			} else {
				return cb.lessThanOrEqualTo(session.get("startTime"), end);
			}
		};
	}
}