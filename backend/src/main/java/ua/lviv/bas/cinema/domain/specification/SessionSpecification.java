package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
import ua.lviv.bas.cinema.util.SpecificationUtils;

@Component
public class SessionSpecification {

	public Specification<Session> build(SessionFilterRequest filter) {
		return Specification.allOf(filterByStatus(filter.getStatus()), filterByHall(filter.getHallId()),
				filterByMovie(filter.getMovieId()), filterByDateRange(filter.getDateFrom(), filter.getDateTo()));
	}

	private Specification<Session> filterByStatus(CinemaSessionStatus status) {
		return SpecificationUtils.equalIfNotNull("status", status);
	}

	private Specification<Session> filterByHall(Long hallId) {
		return SpecificationUtils.equalInJoinIfNotNull("hall", "id", hallId);
	}

	private Specification<Session> filterByMovie(Long movieId) {
		return SpecificationUtils.equalInJoinIfNotNull("movie", "id", movieId);
	}

	private Specification<Session> filterByDateRange(LocalDate from, LocalDate to) {
		return SpecificationUtils.betweenDatesIfNotNull("startTime", from, to);
	}
}