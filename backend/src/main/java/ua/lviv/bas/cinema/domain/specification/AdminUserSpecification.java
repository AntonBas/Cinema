package ua.lviv.bas.cinema.domain.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.domain.projection.AdminUserProjection;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;

@Component
public class AdminUserSpecification {

	public Specification<AdminUserProjection> build(UserFilterRequest filter) {
		return Specification.allOf(searchByText(filter.getSearch()), filterByRole(filter.getRole()),
				filterByVerificationStatus(filter.getVerificationStatus()), filterByEnabled(filter.getEnabled()));
	}

	private Specification<AdminUserProjection> searchByText(String search) {
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

	private Specification<AdminUserProjection> filterByRole(UserRole role) {
		return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("userRole"), role);
	}

	private Specification<AdminUserProjection> filterByVerificationStatus(VerificationStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction()
				: cb.equal(root.get("verificationStatus"), status);
	}

	private Specification<AdminUserProjection> filterByEnabled(Boolean enabled) {
		return (root, query, cb) -> enabled == null ? cb.conjunction() : cb.equal(root.get("enabled"), enabled);
	}
}