package ua.lviv.bas.cinema.domain.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;

@Component
public class UserSpecification {

	public Specification<User> buildForAdmin(UserFilterRequest filter) {
		return Specification.allOf(searchByText(filter.getSearch()), filterByRole(filter.getRole()),
				filterByVerificationStatus(filter.getVerificationStatus()), filterByEnabled(filter.getEnabled()));
	}

	private Specification<User> searchByText(String search) {
		return (root, query, cb) -> {
			if (search == null || search.trim().isEmpty()) {
				return cb.conjunction();
			}
			String pattern = "%" + search.toLowerCase() + "%";
			return cb.or(cb.like(cb.lower(root.get("email")), pattern),
					cb.like(cb.lower(root.get("firstName")), pattern),
					cb.like(cb.lower(root.get("lastName")), pattern));
		};
	}

	private Specification<User> filterByRole(UserRole role) {
		return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("userRole"), role);
	}

	private Specification<User> filterByVerificationStatus(VerificationStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction()
				: cb.equal(root.get("verificationStatus"), status);
	}

	private Specification<User> filterByEnabled(Boolean enabled) {
		return (root, query, cb) -> enabled == null ? cb.conjunction() : cb.equal(root.get("enabled"), enabled);
	}
}