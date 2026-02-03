package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.util.SpecificationUtils;

@Component
public class TicketSpecification {

	public Specification<Ticket> build(TicketFilterRequest filter) {
		return Specification.allOf(filterByUserId(filter.getUserId()), filterByStatus(filter.getStatus()),
				filterByPurchaseDateFrom(filter.getPurchaseDateFrom()),
				filterByPurchaseDateTo(filter.getPurchaseDateTo()),
				filterBySessionDateFrom(filter.getSessionDateFrom()), filterBySessionDateTo(filter.getSessionDateTo()),
				filterByMovieId(filter.getMovieId()));
	}

	private Specification<Ticket> filterByUserId(Long userId) {
		return SpecificationUtils.equalIfNotNull("user.id", userId);
	}

	private Specification<Ticket> filterByStatus(TicketStatus status) {
		return SpecificationUtils.equalIfNotNull("status", status);
	}

	private Specification<Ticket> filterByPurchaseDateFrom(LocalDate dateFrom) {
		return SpecificationUtils.dateFromIfNotNull("purchaseTime", dateFrom);
	}

	private Specification<Ticket> filterByPurchaseDateTo(LocalDate dateTo) {
		return SpecificationUtils.dateToIfNotNull("purchaseTime", dateTo);
	}

	private Specification<Ticket> filterBySessionDateFrom(LocalDate dateFrom) {
		return SpecificationUtils.nestedDateFromIfNotNull("booking.session.startTime", dateFrom);
	}

	private Specification<Ticket> filterBySessionDateTo(LocalDate dateTo) {
		return SpecificationUtils.nestedDateToIfNotNull("booking.session.startTime", dateTo);
	}

	private Specification<Ticket> filterByMovieId(Long movieId) {
		return SpecificationUtils.nestedEqualIfNotNull("booking.session.movie.id", movieId);
	}
}