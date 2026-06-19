package ua.lviv.bas.cinema.repository.cinema.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MovieSpecification {

    public Specification<Movie> forMovies(String query, MovieStatus status) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Movie> forPublicListing(String query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("status").in(MovieStatus.CURRENT, MovieStatus.UPCOMING));
            predicates.add(cb.greaterThanOrEqualTo(root.get("endShowingDate"), LocalDate.now()));

            if (query != null && !query.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Movie> byDate(LocalDate date) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("status").in(MovieStatus.CURRENT, MovieStatus.UPCOMING));
            predicates.add(cb.greaterThanOrEqualTo(root.get("endShowingDate"), date));
            predicates.add(cb.lessThanOrEqualTo(root.get("releaseDate"), date));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Movie> byDateAndTitle(LocalDate date, String title) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("status").in(MovieStatus.CURRENT, MovieStatus.UPCOMING));
            predicates.add(cb.greaterThanOrEqualTo(root.get("endShowingDate"), LocalDate.now()));
            predicates.add(cb.between(cb.literal(date), root.get("releaseDate"), root.get("endShowingDate")));

            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Movie> currentMovies() {
        return (root, cq, cb) -> {
            LocalDate now = LocalDate.now();
            return cb.and(
                    cb.equal(root.get("status"), MovieStatus.CURRENT),
                    cb.lessThanOrEqualTo(root.get("releaseDate"), now),
                    cb.greaterThanOrEqualTo(root.get("endShowingDate"), now)
            );
        };
    }

    public Specification<Movie> upcomingMovies() {
        return (root, cq, cb) -> cb.and(
                cb.equal(root.get("status"), MovieStatus.UPCOMING),
                cb.greaterThan(root.get("releaseDate"), LocalDate.now())
        );
    }

    public Specification<Movie> leavingSoonMovies() {
        return (root, cq, cb) -> {
            LocalDate now = LocalDate.now();
            return cb.and(
                    cb.equal(root.get("status"), MovieStatus.CURRENT),
                    cb.between(root.get("endShowingDate"), now, now.plusDays(7))
            );
        };
    }
}