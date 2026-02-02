package ua.lviv.bas.cinema.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;

public class SpecificationUtils {
	private SpecificationUtils() {
	}

	public static <T> Specification<T> joinFetch(String attribute, JoinType joinType) {
		return (root, query, cb) -> {
			if (!Long.class.equals(query.getResultType())) {
				root.fetch(attribute, joinType);
			}
			return cb.conjunction();
		};
	}

	public static <T> Specification<T> distinct() {
		return (root, query, cb) -> {
			query.distinct(true);
			return cb.conjunction();
		};
	}

	public static <T> Specification<T> equalIfNotNull(String field, Object value) {
		return (root, query, cb) -> {
			if (value == null) {
				return cb.conjunction();
			}
			return cb.equal(root.get(field), value);
		};
	}

	public static <T> Specification<T> equalInJoinIfNotNull(String joinField, String field, Object value) {
		return (root, query, cb) -> {
			if (value == null) {
				return cb.conjunction();
			}
			Join<T, ?> join = root.join(joinField, JoinType.INNER);
			return cb.equal(join.get(field), value);
		};
	}

	public static <T> Specification<T> likeIfNotNull(String field, String value) {
		return (root, query, cb) -> {
			if (value == null || value.trim().isEmpty()) {
				return cb.conjunction();
			}
			return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
		};
	}

	public static <T> Specification<T> likeInJoinIfNotNull(String joinField, String field, String value) {
		return (root, query, cb) -> {
			if (value == null || value.trim().isEmpty()) {
				return cb.conjunction();
			}
			Join<T, ?> join = root.join(joinField, JoinType.INNER);
			return cb.like(cb.lower(join.get(field)), "%" + value.toLowerCase() + "%");
		};
	}

	public static <T> Specification<T> dateFromIfNotNull(String field, LocalDate date) {
		return (root, query, cb) -> {
			if (date == null) {
				return cb.conjunction();
			}
			return cb.greaterThanOrEqualTo(root.get(field), date.atStartOfDay());
		};
	}

	public static <T> Specification<T> dateToIfNotNull(String field, LocalDate date) {
		return (root, query, cb) -> {
			if (date == null) {
				return cb.conjunction();
			}
			return cb.lessThanOrEqualTo(root.get(field), date.atTime(LocalTime.MAX));
		};
	}

	public static <T> Specification<T> greaterThanIfNotNull(String field, Number value) {
		return (root, query, cb) -> {
			if (value == null) {
				return cb.conjunction();
			}
			return cb.gt(root.get(field), value);
		};
	}

	public static <T> Specification<T> lessThanIfNotNull(String field, Number value) {
		return (root, query, cb) -> {
			if (value == null) {
				return cb.conjunction();
			}
			return cb.lt(root.get(field), value);
		};
	}

	public static <T> Specification<T> betweenNumbersIfNotNull(String field, Number min, Number max) {
		return (root, query, cb) -> {
			if (min == null && max == null) {
				return cb.conjunction();
			}

			Path<Number> numberPath = root.get(field);

			if (min != null && max != null) {
				return cb.and(cb.ge(numberPath, min), cb.le(numberPath, max));
			} else if (min != null) {
				return cb.ge(numberPath, min);
			} else {
				return cb.le(numberPath, max);
			}
		};
	}

	public static <T> Specification<T> inIfNotEmpty(String field, Collection<?> values) {
		return (root, query, cb) -> {
			if (values == null || values.isEmpty()) {
				return cb.conjunction();
			}
			return root.get(field).in(values);
		};
	}

	public static <T> Specification<T> isNull(String field) {
		return (root, query, cb) -> cb.isNull(root.get(field));
	}

	public static <T> Specification<T> isNotNull(String field) {
		return (root, query, cb) -> cb.isNotNull(root.get(field));
	}

	public static <T> Specification<T> betweenDatesIfNotNull(String field, LocalDate from, LocalDate to) {
		return (root, query, cb) -> {
			if (from == null && to == null) {
				return cb.conjunction();
			}

			Path<LocalDateTime> datePath = root.get(field);

			if (from != null && to != null) {
				return cb.and(cb.greaterThanOrEqualTo(datePath, from.atStartOfDay()),
						cb.lessThanOrEqualTo(datePath, to.atTime(LocalTime.MAX)));
			} else if (from != null) {
				return cb.greaterThanOrEqualTo(datePath, from.atStartOfDay());
			} else {
				return cb.lessThanOrEqualTo(datePath, to.atTime(LocalTime.MAX));
			}
		};
	}
}