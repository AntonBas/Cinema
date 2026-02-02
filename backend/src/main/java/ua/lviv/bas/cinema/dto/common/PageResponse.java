package ua.lviv.bas.cinema.dto.common;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
	private List<T> content;
	private int number;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;
	private boolean empty;
	private boolean hasNext;
	private boolean hasPrevious;
	private int numberOfElements;
	private List<SortInfo> sort;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SortInfo {
		private String property;
		private String direction;
		private boolean ascending;
	}

	public static <T> PageResponse<T> from(Page<T> page) {
		return PageResponse.<T>builder().content(page.getContent()).number(page.getNumber()).size(page.getSize())
				.totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).first(page.isFirst())
				.last(page.isLast()).empty(page.isEmpty()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
				.numberOfElements(page.getNumberOfElements()).sort(extractSortInfo(page.getSort())).build();
	}

	private static List<SortInfo> extractSortInfo(Sort sort) {
		return sort.stream()
				.map(order -> SortInfo.builder().property(order.getProperty())
						.direction(order.getDirection().name().toLowerCase()).ascending(order.isAscending()).build())
				.collect(Collectors.toList());
	}
}