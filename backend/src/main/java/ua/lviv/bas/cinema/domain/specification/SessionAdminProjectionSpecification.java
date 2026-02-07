package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
import ua.lviv.bas.cinema.util.SpecificationUtils;

@Component
public class SessionAdminProjectionSpecification {

	public Specification<SessionAdminProjection> build(SessionFilterRequest filter) {
		return Specification.allOf(filterByStatus(filter.getStatus()), filterByHall(filter.getHallId()),
				filterByMovie(filter.getMovieId()), filterByDateRange(filter.getDateFrom(), filter.getDateTo()));
	}

	private Specification<SessionAdminProjection> filterByStatus(CinemaSessionStatus status) {
		return SpecificationUtils.equalIfNotNull("status", status);
	}

	private Specification<SessionAdminProjection> filterByHall(Long hallId) {
		return SpecificationUtils.equalIfNotNull("hallId", hallId);
	}

	private Specification<SessionAdminProjection> filterByMovie(Long movieId) {
		return SpecificationUtils.equalIfNotNull("movieId", movieId);
	}

	private Specification<SessionAdminProjection> filterByDateRange(LocalDate from, LocalDate to) {
		return SpecificationUtils.betweenDatesIfNotNull("startTime", from, to);
	}
}