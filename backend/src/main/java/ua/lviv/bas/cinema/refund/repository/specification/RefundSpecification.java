package ua.lviv.bas.cinema.refund.repository.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ua.lviv.bas.cinema.common.CinemaTime;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;

@Component
public class RefundSpecification {

    private static final List<RefundStatus> NEEDS_ATTENTION = List.of(RefundStatus.PROCESSING,
            RefundStatus.REJECTED);

    private static final Pattern BOOKING_NUMBER = Pattern.compile("^BK-\\d{4}-0*(\\d{1,18})$",
            Pattern.CASE_INSENSITIVE);

    public Specification<Refund> forAdmin(String search, RefundStatus status, boolean needsAttention, Long userId,
                                          LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                predicates.add(matchesSearch(root, cb, search.trim()));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (needsAttention) {
                predicates.add(root.get("status").in(NEEDS_ATTENTION));
            }

            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"),
                        dateFrom.atStartOfDay(CinemaTime.ZONE).toInstant()));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThan(root.get("createdDate"),
                        dateTo.plusDays(1).atStartOfDay(CinemaTime.ZONE).toInstant()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Predicate matchesSearch(Root<Refund> root, CriteriaBuilder cb, String search) {
        Matcher bookingNumber = BOOKING_NUMBER.matcher(search);
        if (bookingNumber.matches()) {
            return cb.equal(root.get("payment").get("booking").get("id"), Long.parseLong(bookingNumber.group(1)));
        }

        String pattern = "%" + search.toLowerCase() + "%";
        return cb.or(
                cb.like(cb.lower(root.join("user").get("email")), pattern),
                cb.like(cb.lower(root.join("payment").get("liqpayOrderId")), pattern),
                cb.like(cb.lower(root.join("ticket", JoinType.LEFT).get("uniqueCode")), pattern));
    }
}
