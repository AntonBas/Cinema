package ua.lviv.bas.cinema.dto.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard paginated response wrapper for endpoints that return lists of data")
public class PageResponse<T> {

	@Schema(description = "List of items on the current page")
	@JsonProperty("content")
	private List<T> content;

	@Schema(description = "Current page number (0-based)", example = "0")
	@JsonProperty("page")
	private int currentPage;

	@Schema(description = "Total number of pages", example = "5")
	@JsonProperty("totalPages")
	private int totalPages;

	@Schema(description = "Total number of elements across all pages", example = "150")
	@JsonProperty("totalElements")
	private long totalElements;

	@Schema(description = "Number of items per page", example = "20")
	@JsonProperty("size")
	private int pageSize;

	@Schema(description = "Indicates if this is the first page", example = "true")
	@JsonProperty("first")
	private boolean first;

	@Schema(description = "Indicates if this is the last page", example = "false")
	@JsonProperty("last")
	private boolean last;

	@Schema(description = "Indicates if the page content is empty", example = "false")
	@JsonProperty("empty")
	private boolean empty;

	@Schema(hidden = true)
	public static <T> PageResponse<T> of(Page<T> page) {
		return PageResponse.<T>builder().content(page.getContent()).currentPage(page.getNumber())
				.totalPages(page.getTotalPages()).totalElements(page.getTotalElements()).pageSize(page.getSize())
				.first(page.isFirst()).last(page.isLast()).empty(page.isEmpty()).build();
	}

	@Schema(hidden = true)
	public static <T, R> PageResponse<R> of(Page<T> page, java.util.function.Function<T, R> mapper) {
		List<R> content = page.getContent().stream().map(mapper).toList();

		return PageResponse.<R>builder().content(content).currentPage(page.getNumber()).totalPages(page.getTotalPages())
				.totalElements(page.getTotalElements()).pageSize(page.getSize()).first(page.isFirst())
				.last(page.isLast()).empty(page.isEmpty()).build();
	}
}