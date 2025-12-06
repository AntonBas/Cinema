package ua.lviv.bas.cinema.dto.shared;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

	@JsonProperty("content")
	private List<T> content;

	@JsonProperty("page")
	private int currentPage;

	@JsonProperty("totalPages")
	private int totalPages;

	@JsonProperty("totalElements")
	private long totalElements;

	@JsonProperty("size")
	private int pageSize;

	@JsonProperty("first")
	private boolean first;

	@JsonProperty("last")
	private boolean last;

	@JsonProperty("empty")
	private boolean empty;

	public static <T> PageResponse<T> of(Page<T> page) {
		return PageResponse.<T>builder().content(page.getContent()).currentPage(page.getNumber())
				.totalPages(page.getTotalPages()).totalElements(page.getTotalElements()).pageSize(page.getSize())
				.first(page.isFirst()).last(page.isLast()).empty(page.isEmpty()).build();
	}

	public static <T, R> PageResponse<R> of(Page<T> page, java.util.function.Function<T, R> mapper) {
		List<R> content = page.getContent().stream().map(mapper).toList();

		return PageResponse.<R>builder().content(content).currentPage(page.getNumber()).totalPages(page.getTotalPages())
				.totalElements(page.getTotalElements()).pageSize(page.getSize()).first(page.isFirst())
				.last(page.isLast()).empty(page.isEmpty()).build();
	}
}