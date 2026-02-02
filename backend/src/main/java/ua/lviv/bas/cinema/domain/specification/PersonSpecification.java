package ua.lviv.bas.cinema.domain.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonFilterRequest;
import ua.lviv.bas.cinema.util.SpecificationUtils;

@Component
public class PersonSpecification {

	public Specification<Person> build(PersonFilterRequest filter) {
		return Specification.allOf(filterByName(filter.getName()), filterByRole(filter.getRole()));
	}

	private Specification<Person> filterByName(String name) {
		return SpecificationUtils.likeIfNotNull("name", name);
	}

	private Specification<Person> filterByRole(PersonRole role) {
		return SpecificationUtils.equalIfNotNull("role", role);
	}

	public Specification<Person> buildForSearch(String searchTerm) {
		return (root, query, cb) -> {
			if (!StringUtils.hasText(searchTerm)) {
				return null;
			}

			String pattern = "%" + searchTerm.toLowerCase() + "%";

			return cb.or(cb.like(cb.lower(root.get("name")), pattern));
		};
	}
}