package ua.lviv.bas.cinema.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class FixedOrderPageable {

    private FixedOrderPageable() {
    }

    public static Pageable of(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }
}
