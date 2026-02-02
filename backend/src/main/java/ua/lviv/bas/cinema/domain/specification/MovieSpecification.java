package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;
import ua.lviv.bas.cinema.util.SpecificationUtils;

@Component
public class MovieSpecification {

	public Specification<Movie> build(MovieFilterRequest filter) {
		return Specification.allOf(filterByTitle(filter.getTitle()), filterByStatus(filter.getStatus()),
				filterByAgeRating(filter.getAgeRating()), filterByCurrentlyShowing(filter.getCurrentlyShowing()),
				filterByUpcoming(filter.getUpcoming()), filterByReleaseDateFrom(filter.getReleaseDateFrom()),
				filterByReleaseDateTo(filter.getReleaseDateTo()), filterByGenreId(filter.getGenreId()),
				filterByActorId(filter.getActorId()), filterByDirectorId(filter.getDirectorId()),
				filterByScreenwriterId(filter.getScreenwriterId()), applyDistinct(filter));
	}

	public Specification<Movie> buildWithJoins(MovieFilterRequest filter) {
		return build(filter).and(fetchJoins());
	}

	private Specification<Movie> filterByTitle(String title) {
		return SpecificationUtils.likeIfNotNull("title", title);
	}

	private Specification<Movie> filterByStatus(MovieStatus status) {
		return SpecificationUtils.equalIfNotNull("status", status);
	}

	private Specification<Movie> filterByAgeRating(AgeRating ageRating) {
		return SpecificationUtils.equalIfNotNull("ageRating", ageRating);
	}

	private Specification<Movie> filterByCurrentlyShowing(Boolean currentlyShowing) {
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

	private Specification<Movie> filterByUpcoming(Boolean upcoming) {
		return (root, query, cb) -> {
			if (upcoming == null || !upcoming) {
				return null;
			}
			LocalDate today = LocalDate.now();
			return cb.and(cb.greaterThan(root.get("releaseDate"), today),
					cb.equal(root.get("status"), MovieStatus.UPCOMING));
		};
	}

	private Specification<Movie> filterByReleaseDateFrom(LocalDate dateFrom) {
		return SpecificationUtils.dateFromIfNotNull("releaseDate", dateFrom);
	}

	private Specification<Movie> filterByReleaseDateTo(LocalDate dateTo) {
		return SpecificationUtils.dateToIfNotNull("releaseDate", dateTo);
	}

	private Specification<Movie> filterByGenreId(Long genreId) {
		return (root, query, cb) -> {
			if (genreId == null) {
				return null;
			}
			Subquery<Long> subquery = query.subquery(Long.class);
			var subRoot = subquery.from(Movie.class);
			subquery.select(cb.literal(1L)).where(cb.and(cb.equal(subRoot.get("id"), root.get("id")),
					cb.equal(subRoot.join("genres").get("id"), genreId)));
			return cb.exists(subquery);
		};
	}

	private Specification<Movie> filterByActorId(Long actorId) {
		return (root, query, cb) -> {
			if (actorId == null) {
				return null;
			}
			Subquery<Long> subquery = query.subquery(Long.class);
			var subRoot = subquery.from(Movie.class);
			subquery.select(cb.literal(1L)).where(cb.and(cb.equal(subRoot.get("id"), root.get("id")),
					cb.equal(subRoot.join("actors").get("id"), actorId)));
			return cb.exists(subquery);
		};
	}

	private Specification<Movie> filterByDirectorId(Long directorId) {
		return (root, query, cb) -> {
			if (directorId == null) {
				return null;
			}
			Subquery<Long> subquery = query.subquery(Long.class);
			var subRoot = subquery.from(Movie.class);
			subquery.select(cb.literal(1L)).where(cb.and(cb.equal(subRoot.get("id"), root.get("id")),
					cb.equal(subRoot.join("directors").get("id"), directorId)));
			return cb.exists(subquery);
		};
	}

	private Specification<Movie> filterByScreenwriterId(Long screenwriterId) {
		return (root, query, cb) -> {
			if (screenwriterId == null) {
				return null;
			}
			Subquery<Long> subquery = query.subquery(Long.class);
			var subRoot = subquery.from(Movie.class);
			subquery.select(cb.literal(1L)).where(cb.and(cb.equal(subRoot.get("id"), root.get("id")),
					cb.equal(subRoot.join("screenwriters").get("id"), screenwriterId)));
			return cb.exists(subquery);
		};
	}

	private Specification<Movie> applyDistinct(MovieFilterRequest filter) {
		return (root, query, cb) -> {
			if (hasJoinFilters(filter)) {
				query.distinct(true);
			}
			return cb.conjunction();
		};
	}

	private Specification<Movie> fetchJoins() {
		return (root, query, cb) -> {
			if (query.getResultType() != Long.class && query.getResultType() != long.class) {
				root.fetch("genres", JoinType.LEFT);
				root.fetch("actors", JoinType.LEFT);
				root.fetch("directors", JoinType.LEFT);
				root.fetch("screenwriters", JoinType.LEFT);
			}
			return cb.conjunction();
		};
	}

	private boolean hasJoinFilters(MovieFilterRequest filter) {
		return filter.getGenreId() != null || filter.getActorId() != null || filter.getDirectorId() != null
				|| filter.getScreenwriterId() != null;
	}
}