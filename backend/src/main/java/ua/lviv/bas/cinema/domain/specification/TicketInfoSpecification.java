package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.domain.projection.TicketInfoProjection;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;

@Component
public class TicketInfoSpecification {

	public Specification<TicketInfoProjection> build(Long userId, TicketFilterRequest filter) {
		return Specification.allOf(filterByUserId(userId), filterByStatus(filter.getStatus()),
				filterByPurchaseDateFrom(filter.getPurchaseDateFrom()),
				filterByPurchaseDateTo(filter.getPurchaseDateTo()),
				filterBySessionDateFrom(filter.getSessionDateFrom()), filterBySessionDateTo(filter.getSessionDateTo()),
				filterByMovieId(filter.getMovieId()));
	}

	private Specification<TicketInfoProjection> filterByUserId(Long userId) {
		return (root, query, cb) -> cb.equal(root.get("userId"), userId);
	}

	private Specification<TicketInfoProjection> filterByStatus(TicketStatus status) {
		return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
	}

	private Specification<TicketInfoProjection> filterByPurchaseDateFrom(LocalDate dateFrom) {
		return (root, query, cb) -> {
			if (dateFrom == null) {
				return null;
			}
			LocalDateTime startOfDay = dateFrom.atStartOfDay();
			return cb.greaterThanOrEqualTo(root.get("purchaseTime"), startOfDay);
		};
	}

	private Specification<TicketInfoProjection> filterByPurchaseDateTo(LocalDate dateTo) {
		return (root, query, cb) -> {
			if (dateTo == null) {
				return null;
			}
			LocalDateTime endOfDay = dateTo.atTime(LocalTime.MAX);
			return cb.lessThanOrEqualTo(root.get("purchaseTime"), endOfDay);
		};
	}

	private Specification<TicketInfoProjection> filterBySessionDateFrom(LocalDate dateFrom) {
		return (root, query, cb) -> {
			if (dateFrom == null) {
				return null;
			}
			LocalDateTime startOfDay = dateFrom.atStartOfDay();
			return cb.greaterThanOrEqualTo(root.get("sessionStartTime"), startOfDay);
		};
	}

	private Specification<TicketInfoProjection> filterBySessionDateTo(LocalDate dateTo) {
		return (root, query, cb) -> {
			if (dateTo == null) {
				return null;
			}
			LocalDateTime endOfDay = dateTo.atTime(LocalTime.MAX);
			return cb.lessThanOrEqualTo(root.get("sessionStartTime"), endOfDay);
		};
	}

	private Specification<TicketInfoProjection> filterByMovieId(Long movieId) {
		return (root, query, cb) -> movieId != null ? cb.equal(root.get("movieId"), movieId) : null;
	}
}