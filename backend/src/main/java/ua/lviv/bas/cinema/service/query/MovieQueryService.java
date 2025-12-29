package ua.lviv.bas.cinema.service.query;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.QMovie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.filter.MovieFilter;
import ua.lviv.bas.cinema.repository.MovieRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieQueryService {

	private final MovieRepository movieRepository;

	public Page<Movie> findFilteredMovies(MovieFilter filter) {
		log.debug("Finding filtered movies with filter: {}", filter);

		validateFilter(filter);

		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		if (StringUtils.hasText(filter.getSearchTerm())) {
			String searchTerm = "%" + filter.getSearchTerm().toLowerCase().trim() + "%";
			predicate.and(qMovie.title.lower().like(searchTerm).or(qMovie.description.lower().like(searchTerm)));
		}

		if (filter.getStatus() != null) {
			predicate.and(qMovie.status.eq(filter.getStatus()));
		} else if (!filter.isAdminView()) {
			predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));
		}

		if (filter.getAgeRating() != null) {
			predicate.and(qMovie.ageRating.eq(filter.getAgeRating()));
		}

		if (filter.getMinDuration() != null) {
			predicate.and(qMovie.durationMinutes.goe(filter.getMinDuration()));
		}
		if (filter.getMaxDuration() != null) {
			predicate.and(qMovie.durationMinutes.loe(filter.getMaxDuration()));
		}

		if (filter.getReleaseDateFrom() != null) {
			predicate.and(qMovie.releaseDate.goe(filter.getReleaseDateFrom()));
		}
		if (filter.getReleaseDateTo() != null) {
			predicate.and(qMovie.releaseDate.loe(filter.getReleaseDateTo()));
		}

		Sort sort = Sort.by(
				filter.getSortDirection() == MovieFilter.SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC,
				filter.getSortBy());

		Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
		return movieRepository.findAll(predicate, pageable);
	}

	public Page<Movie> findByTitle(String search, Pageable pageable, boolean adminView) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		if (StringUtils.hasText(search)) {
			String searchTerm = "%" + search.toLowerCase() + "%";
			predicate.and(qMovie.title.lower().like(searchTerm));
		}

		if (!adminView) {
			predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));
		}

		return movieRepository.findAll(predicate, pageable);
	}

	public Page<Movie> findByStatus(MovieStatus status, Pageable pageable, boolean adminView) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(qMovie.status.eq(status));

		if (!adminView && status == MovieStatus.ARCHIVED) {
			throw new IllegalArgumentException("Users cannot view archived movies");
		}

		return movieRepository.findAll(predicate, pageable);
	}

	public Page<Movie> findByAgeRating(ua.lviv.bas.cinema.domain.enums.AgeRating ageRating, Pageable pageable,
			boolean adminView) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(qMovie.ageRating.eq(ageRating));

		if (!adminView) {
			predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));
		}

		return movieRepository.findAll(predicate, pageable);
	}

	public Page<Movie> findCurrentlyShowing(Pageable pageable, boolean adminView) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		predicate.and(qMovie.releaseDate.loe(today));
		predicate.and(qMovie.endShowingDate.goe(today));

		if (!adminView) {
			predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));
		}

		return movieRepository.findAll(predicate, pageable);
	}

	public Page<Movie> findUpcoming(Pageable pageable, boolean adminView) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		predicate.and(qMovie.releaseDate.gt(today));

		if (!adminView) {
			predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));
		}

		return movieRepository.findAll(predicate, pageable);
	}

	public Page<Movie> findArchived(Pageable pageable) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(qMovie.status.eq(MovieStatus.ARCHIVED));

		return movieRepository.findAll(predicate, pageable);
	}

	public Page<Movie> findAvailableMovies(LocalDate fromDate, LocalDate toDate, Pageable pageable, boolean adminView) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(qMovie.releaseDate.loe(toDate));
		predicate.and(qMovie.endShowingDate.goe(fromDate));

		if (!adminView) {
			predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));
		}

		return movieRepository.findAll(predicate, pageable);
	}

	public List<Movie> findMoviesForSessionCreation(String searchTerm, LocalDate sessionDate) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		if (StringUtils.hasText(searchTerm)) {
			String term = "%" + searchTerm.toLowerCase().trim() + "%";
			predicate.and(qMovie.title.lower().like(term));
		}

		predicate.and(qMovie.releaseDate.loe(sessionDate));
		predicate.and(qMovie.endShowingDate.goe(sessionDate));
		predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));

		Pageable pageable = PageRequest.of(0, 20, Sort.by("title").ascending());
		return movieRepository.findAll(predicate, pageable).getContent();
	}

	public List<Movie> findCurrentlyShowingList() {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		predicate.and(qMovie.releaseDate.loe(today));
		predicate.and(qMovie.endShowingDate.goe(today));
		predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));

		Pageable pageable = PageRequest.of(0, 50, Sort.by("releaseDate").descending());
		return movieRepository.findAll(predicate, pageable).getContent();
	}

	public List<Movie> findUpcomingList() {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		predicate.and(qMovie.releaseDate.gt(today));
		predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));

		Pageable pageable = PageRequest.of(0, 50, Sort.by("releaseDate").ascending());
		return movieRepository.findAll(predicate, pageable).getContent();
	}

	public List<Movie> findNewReleases(int limit) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		LocalDate monthAgo = today.minusMonths(1);

		predicate.and(qMovie.releaseDate.between(monthAgo, today));
		predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));

		Pageable pageable = PageRequest.of(0, limit, Sort.by("releaseDate").descending());
		return movieRepository.findAll(predicate, pageable).getContent();
	}

	public List<Movie> findEndingSoon(int limit) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		LocalDate weekLater = today.plusWeeks(1);

		predicate.and(qMovie.endShowingDate.between(today, weekLater));
		predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));

		Pageable pageable = PageRequest.of(0, limit, Sort.by("endShowingDate").ascending());
		return movieRepository.findAll(predicate, pageable).getContent();
	}

	public long countCurrentlyShowing() {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		predicate.and(qMovie.releaseDate.loe(today));
		predicate.and(qMovie.endShowingDate.goe(today));
		predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));

		return movieRepository.count(predicate);
	}

	public long countUpcoming() {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		LocalDate today = LocalDate.now();
		predicate.and(qMovie.releaseDate.gt(today));
		predicate.and(qMovie.status.ne(MovieStatus.ARCHIVED));

		return movieRepository.count(predicate);
	}

	public boolean existsBySlug(String slug, Long excludeId) {
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();

		predicate.and(qMovie.slug.eq(slug));

		if (excludeId != null) {
			predicate.and(qMovie.id.ne(excludeId));
		}

		return movieRepository.exists(predicate);
	}

	private void validateFilter(MovieFilter filter) {
		if (filter.getReleaseDateFrom() != null && filter.getReleaseDateTo() != null
				&& filter.getReleaseDateFrom().isAfter(filter.getReleaseDateTo())) {
			throw new IllegalArgumentException("releaseDateFrom cannot be after releaseDateTo");
		}

		if (filter.getMinDuration() != null && filter.getMaxDuration() != null
				&& filter.getMinDuration() > filter.getMaxDuration()) {
			throw new IllegalArgumentException("minDuration cannot be greater than maxDuration");
		}

		if (!filter.isAdminView() && filter.getStatus() == MovieStatus.ARCHIVED) {
			throw new IllegalArgumentException("Users cannot filter by ARCHIVED status");
		}

		if (filter.getSize() > 100) {
			throw new IllegalArgumentException("Page size cannot exceed 100");
		}
	}
}