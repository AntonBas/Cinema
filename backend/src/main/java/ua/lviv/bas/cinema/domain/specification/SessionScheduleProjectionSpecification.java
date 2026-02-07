package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;
import ua.lviv.bas.cinema.util.SpecificationUtils;

@Component
public class SessionScheduleProjectionSpecification {

	public Specification<SessionScheduleProjection> build(SessionFilterRequest filter) {
		return Specification.allOf(filterByStatus(CinemaSessionStatus.SCHEDULED), filterByFutureStartTime(),
				filterByHall(filter.getHallId()), filterByMovie(filter.getMovieId()),
				filterByDateRange(filter.getDateFrom(), filter.getDateTo()));
	}

	private Specification<SessionScheduleProjection> filterByStatus(CinemaSessionStatus status) {
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	private Specification<SessionScheduleProjection> filterByFutureStartTime() {
		return (root, query, cb) -> cb.greaterThan(root.get("startTime"), LocalDateTime.now());
	}

	private Specification<SessionScheduleProjection> filterByHall(Long hallId) {
		return SpecificationUtils.equalIfNotNull("hallId", hallId);
	}

	private Specification<SessionScheduleProjection> filterByMovie(Long movieId) {
		return SpecificationUtils.equalIfNotNull("movieId", movieId);
	}

	private Specification<SessionScheduleProjection> filterByDateRange(LocalDate from, LocalDate to) {
		return SpecificationUtils.betweenDatesIfNotNull("startTime", from, to);
	}
}