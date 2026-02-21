package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.service.cinema.GenreService;

@Slf4j
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

	private final GenreService genreService;

	@GetMapping("/search")
	public ResponseEntity<List<GenreResponse>> searchGenres(@RequestParam String query,
			@RequestParam(defaultValue = "10") int limit) {

		log.info("GET /api/genres/search - query: '{}', limit: {}", query, limit);

		Pageable pageable = PageRequest.of(0, limit);
		var result = genreService.searchGenres(query, pageable);

		return ResponseEntity.ok(result.getContent());
	}
}