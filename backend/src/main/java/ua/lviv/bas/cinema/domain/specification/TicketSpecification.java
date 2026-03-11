package ua.lviv.bas.cinema.domain.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Component
public class TicketSpecification {

	public Specification<Ticket> buildForUser(Long userId, TicketStatus status, String movieTitle) {
		return Specification.allOf(filterByUserId(userId), filterByStatus(status), filterByMovieTitle(movieTitle));
	}

	private Specification<Ticket> filterByUserId(Long userId) {
		return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
	}

	private Specification<Ticket> filterByStatus(TicketStatus status) {
		return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
	}

	private Specification<Ticket> filterByMovieTitle(String movieTitle) {
		return (root, query, cb) -> {
			if (!StringUtils.hasText(movieTitle))
				return null;

			Join<Object, Object> booking = root.join("booking", JoinType.INNER);
			Join<Object, Object> session = booking.join("session", JoinType.INNER);
			Join<Object, Object> movie = session.join("movie", JoinType.INNER);

			return cb.like(cb.lower(movie.get("title")), "%" + movieTitle.toLowerCase() + "%");
		};
	}
}