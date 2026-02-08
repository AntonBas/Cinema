package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.projection.MovieCardProjection;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;

@Component
public class MovieCardSpecification {

	public Specification<MovieCardProjection> build(MovieFilterRequest filter) {
		return Specification.allOf(filterByTitle(filter.getTitle()), filterByStatus(filter.getStatus()),
				filterByAgeRating(filter.getAgeRating()), filterByCurrentlyShowing(filter.getCurrentlyShowing()),
				filterByUpcoming(filter.getUpcoming()), filterByReleaseDateFrom(filter.getReleaseDateFrom()),
				filterByReleaseDateTo(filter.getReleaseDateTo()));
	}

	private Specification<MovieCardProjection> filterByTitle(String title) {
		return (root, query, cb) -> {
			if (title == null || title.trim().isEmpty()) {
				return null;
			}
			return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
		};
	}

	private Specification<MovieCardProjection> filterByStatus(MovieStatus status) {
		return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
	}

	private Specification<MovieCardProjection> filterByAgeRating(AgeRating ageRating) {
		return (root, query, cb) -> ageRating != null ? cb.equal(root.get("ageRating"), ageRating) : null;
	}

	private Specification<MovieCardProjection> filterByCurrentlyShowing(Boolean currentlyShowing) {
		return (root, query, cb) -> {
			if (currentlyShowing == null || !currentlyShowing) {
				return null;
			}
			LocalDate today = LocalDate.now();
			return cb.and(cb.lessThanOrEqualTo(root.get("releaseDate"), today),
					cb.greaterThanOrEqualTo(root.get("endShowingDate"), today),
					cb.equal(root.get("status"), MovieStatus.CURRENT));
		};
	}

	private Specification<MovieCardProjection> filterByUpcoming(Boolean upcoming) {
		return (root, query, cb) -> {
			if (upcoming == null || !upcoming) {
				return null;
			}
			LocalDate today = LocalDate.now();
			return cb.and(cb.greaterThan(root.get("releaseDate"), today),
					cb.equal(root.get("status"), MovieStatus.UPCOMING));
		};
	}

	private Specification<MovieCardProjection> filterByReleaseDateFrom(LocalDate dateFrom) {
		return (root, query, cb) -> dateFrom != null ? cb.greaterThanOrEqualTo(root.get("releaseDate"), dateFrom)
				: null;
	}

	private Specification<MovieCardProjection> filterByReleaseDateTo(LocalDate dateTo) {
		return (root, query, cb) -> dateTo != null ? cb.lessThanOrEqualTo(root.get("releaseDate"), dateTo) : null;
	}
}