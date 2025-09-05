package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.CinemaHallCreateDto;
import ua.lviv.bas.cinema.dto.CinemaHallResponseDto;
import ua.lviv.bas.cinema.service.CinemaHallService;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class CinemaHallController {

	private final CinemaHallService cinemaHallService;

	@PostMapping
	public ResponseEntity<CinemaHallResponseDto> createHall(@Valid @RequestBody CinemaHallCreateDto createDto) {
		try {
			CinemaHallResponseDto response = cinemaHallService.createHall(createDto);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<CinemaHallResponseDto> getHallById(@PathVariable Long id) {
		try {
			CinemaHallResponseDto response = cinemaHallService.getHallById(id);
			return ResponseEntity.ok(response);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<CinemaHallResponseDto> updateHall(@PathVariable Long id,
			@Valid @RequestBody CinemaHallCreateDto updateDto) {
		try {
			CinemaHallResponseDto response = cinemaHallService.updateHall(id, updateDto);
			return ResponseEntity.ok(response);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
		try {
			cinemaHallService.deleteHall(id);
			return ResponseEntity.noContent().build();
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping
	public ResponseEntity<List<CinemaHallResponseDto>> getAllHalls() {
		try {
			List<CinemaHallResponseDto> halls = cinemaHallService.getAllHalls();
			return ResponseEntity.ok(halls);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{id}/exists")
	public ResponseEntity<Boolean> checkHallExists(@PathVariable Long id) {
		try {
			boolean exists = cinemaHallService.existsById(id);
			return ResponseEntity.ok(exists);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}