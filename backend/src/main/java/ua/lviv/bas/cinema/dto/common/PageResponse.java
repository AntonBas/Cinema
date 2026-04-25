package ua.lviv.bas.cinema.dto.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

public record PageResponse<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty,
        boolean hasNext,
        boolean hasPrevious,
        int numberOfElements,
        List<SortInfo> sort
) {
    public record SortInfo(
            String property,
            String direction,
            boolean ascending
    ) {
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty(),
                page.hasNext(),
                page.hasPrevious(),
                page.getNumberOfElements(),
                extractSortInfo(page.getSort())
        );
    }

    private static List<SortInfo> extractSortInfo(Sort sort) {
        return sort.stream()
                .map(order -> new SortInfo(
                        order.getProperty(),
                        order.getDirection().name().toLowerCase(),
                        order.isAscending()
                ))
                .collect(Collectors.toList());
    }
}