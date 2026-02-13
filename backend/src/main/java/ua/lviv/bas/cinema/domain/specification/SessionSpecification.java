package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.JoinType;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
import ua.lviv.bas.cinema.util.SpecificationUtils;

@Component
public class SessionSpecification {

	public Specification<Session> buildForAdmin(SessionFilterRequest filter) {
		return Specification.allOf(filterByStatus(filter.getStatus()), filterByHall(filter.getHallId()),
				filterByMovie(filter.getMovieId()), filterByDateRange(filter.getDateFrom(), filter.getDateTo()));
	}

	public Specification<Session> buildForSchedule(SessionFilterRequest filter) {
		return Specification.allOf(filterByStatus(CinemaSessionStatus.SCHEDULED), filterByFutureStartTime(),
				filterByHall(filter.getHallId()), filterByMovie(filter.getMovieId()),
				filterByDateRange(filter.getDateFrom(), filter.getDateTo()));
	}

	private Specification<Session> filterByStatus(CinemaSessionStatus status) {
		return SpecificationUtils.equalIfNotNull("status", status);
	}

	private Specification<Session> filterByHall(Long hallId) {
		return (root, query, cb) -> {
			if (hallId == null)
				return null;
			return cb.equal(root.join("hall", JoinType.INNER).get("id"), hallId);
		};
	}

	private Specification<Session> filterByMovie(Long movieId) {
		return (root, query, cb) -> {
			if (movieId == null)
				return null;
			return cb.equal(root.join("movie", JoinType.INNER).get("id"), movieId);
		};
	}

	private Specification<Session> filterByFutureStartTime() {
		return (root, query, cb) -> cb.greaterThan(root.get("startTime"), LocalDateTime.now());
	}

	private Specification<Session> filterByDateRange(LocalDate from, LocalDate to) {
		return SpecificationUtils.betweenDatesIfNotNull("startTime", from, to);
	}
}