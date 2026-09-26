package ua.lviv.bas.cinema.common;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ua.lviv.bas.cinema.exception.core.InvalidSortPropertyException;

public final class SortWhitelist {

    private final Map<String, String> columnsByProperty;
    private final Sort defaultSort;
    private final Sort tiebreaker;

    private SortWhitelist(Map<String, String> columnsByProperty, Sort defaultSort, Sort tiebreaker) {
        this.columnsByProperty = columnsByProperty;
        this.defaultSort = defaultSort;
        this.tiebreaker = tiebreaker;
    }

    public static SortWhitelist of(Map<String, String> columnsByProperty, Sort defaultSort, Sort tiebreaker) {
        return new SortWhitelist(Map.copyOf(columnsByProperty), defaultSort, tiebreaker);
    }

    public Pageable apply(Pageable pageable) {
        Sort requested = Sort.by(pageable.getSort().stream()
                .map(order -> new Sort.Order(order.getDirection(), resolveColumn(order.getProperty())))
                .toList());
        Sort sort = requested.isUnsorted() ? defaultSort : requested;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(tiebreaker));
    }

    private String resolveColumn(String property) {
        String column = columnsByProperty.get(property);
        if (column == null) {
            throw new InvalidSortPropertyException(property);
        }
        return column;
    }
}
