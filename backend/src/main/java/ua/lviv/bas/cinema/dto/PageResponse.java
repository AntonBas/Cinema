package ua.lviv.bas.cinema.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
	private List<T> content;
	private int currentPage;
	private int totalPages;
	private long totalElements;
	private int pageSize;
}