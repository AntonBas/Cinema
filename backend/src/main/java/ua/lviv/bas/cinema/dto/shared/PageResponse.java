package ua.lviv.bas.cinema.dto.shared;

import java.util.List;

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
	private int currentPage;
	private int totalPages;
	private long totalElements;
	private int pageSize;
}