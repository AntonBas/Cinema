package ua.lviv.bas.cinema.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class FixedOrderPageable {

    private FixedOrderPageable() {
    }

    /**
     * Drops any client-supplied sort so a query with its own ORDER BY stays the single source of ordering.
     */
    public static Pageable of(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }
}
