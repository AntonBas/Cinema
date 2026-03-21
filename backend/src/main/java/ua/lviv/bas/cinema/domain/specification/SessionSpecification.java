package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

	public Specification<Session> buildForSchedule(String searchTerm, LocalDate date) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.equal(root.get("status"), CinemaSessionStatus.SCHEDULED));
			predicates.add(cb.greaterThan(root.get("startTime"), LocalDateTime.now()));

			if (searchTerm != null && !searchTerm.trim().isEmpty()) {
				String pattern = "%" + searchTerm.toLowerCase() + "%";
				predicates.add(cb.like(cb.lower(root.join("movie", JoinType.INNER).get("title")), pattern));
			}

			if (date != null) {
				LocalDateTime startOfDay = date.atStartOfDay();
				LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
				predicates.add(cb.between(root.get("startTime"), startOfDay, endOfDay));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	public Specification<Session> buildForAdmin(SessionFilterRequest filter) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filter.status() != null) {
				predicates.add(cb.equal(root.get("status"), filter.status()));
			}

			if (filter.hallId() != null) {
				predicates.add(cb.equal(root.join("hall", JoinType.INNER).get("id"), filter.hallId()));
			}

			if (filter.movieTitle() != null && !filter.movieTitle().trim().isEmpty()) {
				String pattern = "%" + filter.movieTitle().toLowerCase() + "%";
				predicates.add(cb.like(cb.lower(root.join("movie", JoinType.INNER).get("title")), pattern));
			}

			if (filter.dateFrom() != null || filter.dateTo() != null) {
				addDateRangePredicates(predicates, root, cb, filter.dateFrom(), filter.dateTo());
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private void addDateRangePredicates(List<Predicate> predicates, Root<Session> root, CriteriaBuilder cb,
			LocalDate dateFrom, LocalDate dateTo) {
		if (dateFrom != null && dateTo != null) {
			LocalDateTime fromDateTime = dateFrom.atStartOfDay();
			LocalDateTime toDateTime = dateTo.plusDays(1).atStartOfDay();
			predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), fromDateTime));
			predicates.add(cb.lessThan(root.get("startTime"), toDateTime));
		} else if (dateFrom != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), dateFrom.atStartOfDay()));
		} else if (dateTo != null) {
			predicates.add(cb.lessThan(root.get("startTime"), dateTo.plusDays(1).atStartOfDay()));
		}
	}
}