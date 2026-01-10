package ua.lviv.bas.cinema.service.infrastructure;

import java.text.Normalizer;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.exception.domain.infrastructure.SlugGenerationException;
import ua.lviv.bas.cinema.repository.MovieRepository;

@Service
@RequiredArgsConstructor
public class SlugService {

	private final MovieRepository movieRepository;

	public String generateSlug(String title) {
		if (title == null || title.isBlank()) {
			throw SlugGenerationException.titleRequired();
		}

		String slug = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
				.replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "-").toLowerCase();

		return slug;
	}

	public String generateUniqueSlug(String title) {
		String baseSlug = generateSlug(title);
		String uniqueSlug = baseSlug;
		int counter = 1;

		while (movieRepository.findBySlug(uniqueSlug).isPresent()) {
			uniqueSlug = baseSlug + "-" + counter;
			counter++;
		}

		return uniqueSlug;
	}

	public boolean isSlugAvailable(String slug) {
		return movieRepository.findBySlug(slug).isEmpty();
	}

	public boolean isSlugAvailableForMovie(String slug, Long movieId) {
		Optional<Movie> existingMovie = movieRepository.findBySlug(slug);
		return existingMovie.isEmpty() || existingMovie.get().getId().equals(movieId);
	}
}