package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionFilterRequest;

@Component
public class SessionSpecification {

	public Specification<Session> buildForAdmin(SessionFilterRequest filter) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filter.getStatus() != null) {
				predicates.add(cb.equal(root.get("status"), filter.getStatus()));
			}

			if (filter.getHallId() != null) {
				predicates.add(cb.equal(root.join("hall", JoinType.INNER).get("id"), filter.getHallId()));
			}

			if (filter.getMovieId() != null) {
				predicates.add(cb.equal(root.join("movie", JoinType.INNER).get("id"), filter.getMovieId()));
			}

			if (filter.getDateFrom() != null || filter.getDateTo() != null) {
				addDateRangePredicates(predicates, root, cb, filter.getDateFrom(), filter.getDateTo());
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	public Specification<Session> buildForSchedule(SessionFilterRequest filter) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get("status"), CinemaSessionStatus.SCHEDULED));
			predicates.add(cb.greaterThan(root.get("startTime"), LocalDateTime.now()));

			if (filter.getHallId() != null) {
				predicates.add(cb.equal(root.join("hall", JoinType.INNER).get("id"), filter.getHallId()));
			}

			if (filter.getMovieId() != null) {
				predicates.add(cb.equal(root.join("movie", JoinType.INNER).get("id"), filter.getMovieId()));
			}

			if (filter.getDateFrom() != null || filter.getDateTo() != null) {
				addDateRangePredicates(predicates, root, cb, filter.getDateFrom(), filter.getDateTo());
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private void addDateRangePredicates(List<Predicate> predicates, Root<Session> root, CriteriaBuilder cb,
			LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom != null && dateTo != null) {
			LocalDateTime fromDateTime = dateFrom.atStartOfDay();
			LocalDateTime toDateTime = dateTo.atTime(LocalTime.MAX);
			predicates.add(cb.between(root.get("startTime"), fromDateTime, toDateTime));
		} else if (dateFrom != null) {
			LocalDateTime fromDateTime = dateFrom.atStartOfDay();
			predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), fromDateTime));
		} else if (dateTo != null) {
			LocalDateTime toDateTime = dateTo.atTime(LocalTime.MAX);
			predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), toDateTime));
		}
	}
}