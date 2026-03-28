package ua.lviv.bas.cinema.domain.specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

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

	public static <T> Specification<T> nestedEqualIfNotNull(String nestedField, Object value) {
		return (root, query, cb) -> {
			if (value == null) {
				return cb.conjunction();
			}
			Path<?> path = getNestedPath(root, nestedField);
			return cb.equal(path, value);
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
			Expression<LocalDateTime> dateExpr = root.get(field);
			return cb.greaterThanOrEqualTo(dateExpr, date.atStartOfDay());
		};
	}

	public static <T> Specification<T> nestedDateFromIfNotNull(String nestedField, LocalDate date) {
		return (root, query, cb) -> {
			if (date == null) {
				return cb.conjunction();
			}
			Path<?> path = getNestedPath(root, nestedField);
			return cb.greaterThanOrEqualTo(path.as(LocalDateTime.class), date.atStartOfDay());
		};
	}

	public static <T> Specification<T> dateToIfNotNull(String field, LocalDate date) {
		return (root, query, cb) -> {
			if (date == null) {
				return cb.conjunction();
			}
			Expression<LocalDateTime> dateExpr = root.get(field);
			return cb.lessThanOrEqualTo(dateExpr, date.atTime(LocalTime.MAX));
		};
	}

	public static <T> Specification<T> nestedDateToIfNotNull(String nestedField, LocalDate date) {
		return (root, query, cb) -> {
			if (date == null) {
				return cb.conjunction();
			}
			Path<?> path = getNestedPath(root, nestedField);
			return cb.lessThanOrEqualTo(path.as(LocalDateTime.class), date.atTime(LocalTime.MAX));
		};
	}

	public static <T> Specification<T> greaterThanIfNotNull(String field, Number value) {
		return (root, query, cb) -> {
			if (value == null) {
				return cb.conjunction();
			}
			Expression<Number> numberExpr = root.get(field);
			return cb.gt(numberExpr, value);
		};
	}

	public static <T> Specification<T> lessThanIfNotNull(String field, Number value) {
		return (root, query, cb) -> {
			if (value == null) {
				return cb.conjunction();
			}
			Expression<Number> numberExpr = root.get(field);
			return cb.lt(numberExpr, value);
		};
	}

	public static <T> Specification<T> betweenNumbersIfNotNull(String field, Number min, Number max) {
		return (root, query, cb) -> {
			if (min == null && max == null) {
				return cb.conjunction();
			}

			Expression<Number> numberExpr = root.get(field);

			if (min != null && max != null) {
				return cb.and(cb.ge(numberExpr, min), cb.le(numberExpr, max));
			} else if (min != null) {
				return cb.ge(numberExpr, min);
			} else {
				return cb.le(numberExpr, max);
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

			Expression<LocalDateTime> dateExpr = root.get(field);

			if (from != null && to != null) {
				return cb.and(cb.greaterThanOrEqualTo(dateExpr, from.atStartOfDay()),
						cb.lessThanOrEqualTo(dateExpr, to.atTime(LocalTime.MAX)));
			} else if (from != null) {
				return cb.greaterThanOrEqualTo(dateExpr, from.atStartOfDay());
			} else {
				return cb.lessThanOrEqualTo(dateExpr, to.atTime(LocalTime.MAX));
			}
		};
	}

	public static <T> Specification<T> nestedBetweenDatesIfNotNull(String nestedField, LocalDate from, LocalDate to) {
		return (root, query, cb) -> {
			if (from == null && to == null) {
				return cb.conjunction();
			}

			Path<?> path = getNestedPath(root, nestedField);
			Expression<LocalDateTime> dateExpr = path.as(LocalDateTime.class);

			if (from != null && to != null) {
				return cb.and(cb.greaterThanOrEqualTo(dateExpr, from.atStartOfDay()),
						cb.lessThanOrEqualTo(dateExpr, to.atTime(LocalTime.MAX)));
			} else if (from != null) {
				return cb.greaterThanOrEqualTo(dateExpr, from.atStartOfDay());
			} else {
				return cb.lessThanOrEqualTo(dateExpr, to.atTime(LocalTime.MAX));
			}
		};
	}

	private static <T> Path<?> getNestedPath(Root<T> root, String fieldPath) {
		String[] parts = fieldPath.split("\\.");
		Path<?> path = root.get(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			path = path.get(parts[i]);
		}
		return path;
	}
}