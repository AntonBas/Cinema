package ua.lviv.bas.cinema.movie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.exception.domain.technical.SlugGenerationException;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;

import java.text.Normalizer;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SlugService {

    private static final String FALLBACK_SLUG = "movie";

    private static final Map<Character, String> CYRILLIC_TO_LATIN = Map.ofEntries(
            Map.entry('а', "a"), Map.entry('б', "b"), Map.entry('в', "v"), Map.entry('г', "h"),
            Map.entry('ґ', "g"), Map.entry('д', "d"), Map.entry('е', "e"), Map.entry('є', "ie"),
            Map.entry('ж', "zh"), Map.entry('з', "z"), Map.entry('и', "y"), Map.entry('і', "i"),
            Map.entry('ї', "i"), Map.entry('й', "i"), Map.entry('к', "k"), Map.entry('л', "l"),
            Map.entry('м', "m"), Map.entry('н', "n"), Map.entry('о', "o"), Map.entry('п', "p"),
            Map.entry('р', "r"), Map.entry('с', "s"), Map.entry('т', "t"), Map.entry('у', "u"),
            Map.entry('ф', "f"), Map.entry('х', "kh"), Map.entry('ц', "ts"), Map.entry('ч', "ch"),
            Map.entry('ш', "sh"), Map.entry('щ', "shch"), Map.entry('ь', ""), Map.entry('ю', "iu"),
            Map.entry('я', "ia"), Map.entry('ы', "y"), Map.entry('э', "e"), Map.entry('ё', "e"),
            Map.entry('ъ', ""), Map.entry('\'', ""), Map.entry('’', ""));

    private final MovieRepository movieRepository;

    public String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            throw SlugGenerationException.titleRequired();
        }

        String slug = Normalizer.normalize(transliterate(title), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "").trim().replaceAll("\\s+", "-").toLowerCase();
        return slug.isEmpty() ? FALLBACK_SLUG : slug;
    }

    public String generateUniqueSlug(String title, Long excludeMovieId) {
        var baseSlug = generateSlug(title);
        var uniqueSlug = baseSlug;
        int counter = 1;

        while (!isSlugAvailableForMovie(uniqueSlug, excludeMovieId)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }

        return uniqueSlug;
    }

    public boolean isSlugAvailableForMovie(String slug, Long movieId) {
        Optional<Movie> existingMovie = movieRepository.findBySlug(slug);
        return existingMovie.isEmpty() || existingMovie.get().getId().equals(movieId);
    }

    private String transliterate(String text) {
        var result = new StringBuilder(text.length());
        for (char character : text.toLowerCase().toCharArray()) {
            result.append(CYRILLIC_TO_LATIN.getOrDefault(character, String.valueOf(character)));
        }
        return result.toString();
    }
}
