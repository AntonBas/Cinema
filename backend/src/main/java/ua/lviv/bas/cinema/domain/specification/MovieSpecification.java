package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;

@Component
public class MovieSpecification {

	public Specification<Movie> build(MovieFilterRequest filter) {
		return Specification.allOf(filterByTitle(filter.getTitle()), filterByStatus(filter.getStatus()),
				filterByAgeRating(filter.getAgeRating()), filterByCurrentlyShowing(filter.getCurrentlyShowing()),
				filterByUpcoming(filter.getUpcoming()), filterByArchived(filter.getArchived()),
				filterByReleaseDateFrom(filter.getReleaseDateFrom()), filterByReleaseDateTo(filter.getReleaseDateTo()),
				filterByGenre(filter.getGenreId()), filterByActor(filter.getActorId()),
				filterByDirector(filter.getDirectorId()), filterByScreenwriter(filter.getScreenwriterId()));
	}

	private Specification<Movie> filterByTitle(String title) {
		return (root, query, cb) -> {
			if (title == null || title.trim().isEmpty())
				return null;
			return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
		};
	}

	private Specification<Movie> filterByStatus(MovieStatus status) {
		return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
	}

	private Specification<Movie> filterByAgeRating(AgeRating ageRating) {
		return (root, query, cb) -> ageRating != null ? cb.equal(root.get("ageRating"), ageRating) : null;
	}

	private Specification<Movie> filterByCurrentlyShowing(Boolean currentlyShowing) {
		return (root, query, cb) -> {
			if (currentlyShowing == null || !currentlyShowing)
				return null;
			return cb.equal(root.get("status"), MovieStatus.CURRENT);
		};
	}

	private Specification<Movie> filterByUpcoming(Boolean upcoming) {
		return (root, query, cb) -> {
			if (upcoming == null || !upcoming)
				return null;
			return cb.equal(root.get("status"), MovieStatus.UPCOMING);
		};
	}

	private Specification<Movie> filterByArchived(Boolean archived) {
		return (root, query, cb) -> {
			if (archived == null || !archived)
				return null;
			return cb.equal(root.get("status"), MovieStatus.ARCHIVED);
		};
	}

	private Specification<Movie> filterByReleaseDateFrom(LocalDate dateFrom) {
		return (root, query, cb) -> dateFrom != null ? cb.greaterThanOrEqualTo(root.get("releaseDate"), dateFrom)
				: null;
	}

	private Specification<Movie> filterByReleaseDateTo(LocalDate dateTo) {
		return (root, query, cb) -> dateTo != null ? cb.lessThanOrEqualTo(root.get("releaseDate"), dateTo) : null;
	}

	private Specification<Movie> filterByGenre(Long genreId) {
		return (root, query, cb) -> {
			if (genreId == null)
				return null;
			Join<Movie, Genre> genres = root.join("genres", JoinType.INNER);
			return cb.equal(genres.get("id"), genreId);
		};
	}

	private Specification<Movie> filterByActor(Long actorId) {
		return (root, query, cb) -> {
			if (actorId == null)
				return null;
			Join<Movie, Person> actors = root.join("actors", JoinType.INNER);
			return cb.equal(actors.get("id"), actorId);
		};
	}

	private Specification<Movie> filterByDirector(Long directorId) {
		return (root, query, cb) -> {
			if (directorId == null)
				return null;
			Join<Movie, Person> directors = root.join("directors", JoinType.INNER);
			return cb.equal(directors.get("id"), directorId);
		};
	}

	private Specification<Movie> filterByScreenwriter(Long screenwriterId) {
		return (root, query, cb) -> {
			if (screenwriterId == null)
				return null;
			Join<Movie, Person> screenwriters = root.join("screenwriters", JoinType.INNER);
			return cb.equal(screenwriters.get("id"), screenwriterId);
		};
	}
}