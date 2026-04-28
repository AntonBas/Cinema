package ua.lviv.bas.cinema.service.integration.slug;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.exception.domain.technical.SlugGenerationException;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;

import java.text.Normalizer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SlugService {

    private final MovieRepository movieRepository;

    public String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            throw SlugGenerationException.titleRequired();
        }

        return Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "-").toLowerCase();
    }

    public String generateUniqueSlug(String title) {
        var baseSlug = generateSlug(title);
        var uniqueSlug = baseSlug;
        int counter = 1;

        while (movieRepository.findBySlug(uniqueSlug).isPresent()) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }

        return uniqueSlug;
    }

    public boolean isSlugAvailableForMovie(String slug, Long movieId) {
        Optional<Movie> existingMovie = movieRepository.findBySlug(slug);
        return existingMovie.isEmpty() || existingMovie.get().getId().equals(movieId);
    }
}