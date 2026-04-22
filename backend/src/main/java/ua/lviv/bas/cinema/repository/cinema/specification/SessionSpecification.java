package ua.lviv.bas.cinema.repository.cinema.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;

@Component
public class SessionSpecification {

	public Specification<Session> forSchedule(String searchTerm, LocalDate date) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get("status"), CinemaSessionStatus.SCHEDULED));
			predicates.add(cb.greaterThan(root.get("startTime"), LocalDateTime.now()));

			if (searchTerm != null && !searchTerm.isBlank()) {
				predicates
						.add(cb.like(cb.lower(root.join("movie").get("title")), "%" + searchTerm.toLowerCase() + "%"));
			}

			if (date != null) {
				LocalDateTime startOfDay = date.atStartOfDay();
				LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
				predicates.add(cb.between(root.get("startTime"), startOfDay, endOfDay));
			}

			if (query != null) {
				query.orderBy(cb.asc(root.join("movie").get("title")), cb.asc(root.get("startTime")));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	public Specification<Session> forAdmin(Long hallId, String movieTitle, CinemaSessionStatus status,
			LocalDate dateFrom, LocalDate dateTo) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (hallId != null) {
				predicates.add(cb.equal(root.join("hall").get("id"), hallId));
			}

			if (movieTitle != null && !movieTitle.isBlank()) {
				predicates
						.add(cb.like(cb.lower(root.join("movie").get("title")), "%" + movieTitle.toLowerCase() + "%"));
			}

			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}

			if (dateFrom != null && dateTo != null) {
				predicates.add(
						cb.between(root.get("startTime"), dateFrom.atStartOfDay(), dateTo.plusDays(1).atStartOfDay()));
			} else if (dateFrom != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), dateFrom.atStartOfDay()));
			} else if (dateTo != null) {
				predicates.add(cb.lessThan(root.get("startTime"), dateTo.plusDays(1).atStartOfDay()));
			}

			if (query != null) {
				query.orderBy(
						cb.asc(cb.selectCase().when(cb.equal(root.get("status"), CinemaSessionStatus.SCHEDULED), 1)
								.when(cb.equal(root.get("status"), CinemaSessionStatus.ONGOING), 2)
								.when(cb.equal(root.get("status"), CinemaSessionStatus.CANCELLED), 3).otherwise(4)),
						cb.desc(root.get("startTime")));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}