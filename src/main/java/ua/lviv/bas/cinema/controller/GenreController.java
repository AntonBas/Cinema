package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.GenreDto;
import ua.lviv.bas.cinema.service.GenreService;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

	private final GenreService genreService;

	@GetMapping
	public ResponseEntity<List<GenreDto>> getAllGenres() {
		return ResponseEntity.ok(genreService.getAllGenres());
	}

	@GetMapping("/{id}")
	public ResponseEntity<GenreDto> getGenreById(@PathVariable Long id) {
		GenreDto genre = genreService.readGenre(id);
		return genre != null ? ResponseEntity.ok(genre) : ResponseEntity.notFound().build();
	}

	@PostMapping
	public ResponseEntity<GenreDto> createGenre(@RequestBody @Valid GenreDto genreDto) {
		return ResponseEntity.ok(genreService.createGenre(genreDto));
	}

	@PutMapping("/{id}")
	public ResponseEntity<GenreDto> updateGenre(@PathVariable Long id, @RequestBody @Valid GenreDto genreDto) {
		GenreDto updated = genreService.updateGenre(id, genreDto);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
		genreService.deleteGenre(id);
		return ResponseEntity.noContent().build();
	}
}
