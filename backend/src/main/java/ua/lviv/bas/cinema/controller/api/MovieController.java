package ua.lviv.bas.cinema.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Public APIs for accessing movie information")
public class MovieController {

    private final MovieService movieService;

    @RateLimit(value = 20, duration = 1)
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get movie by slug")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Movie found successfully"),
            @ApiResponse(responseCode = "404", description = "Movie not found")})
    public ResponseEntity<MovieDetailResponse> getMovieBySlug(@PathVariable String slug) {
        log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
        var movie = movieService.getMovieBySlug(slug);

        if (movie.status() == MovieStatus.ARCHIVED) {
            log.warn("Movie with slug {} is archived and not available publicly", slug);
            throw new MovieNotFoundException(slug);
        }

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)).body(movie);
    }

    @RateLimit(value = 20, duration = 1)
    @GetMapping("/currently-showing")
    @Operation(summary = "Get currently showing movies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")})
    public ResponseEntity<PageResponse<MovieCardResponse>> getCurrentlyShowingMovies(
            @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/movies/currently-showing - Getting currently showing movies");
        var result = movieService.getMovies(null, MovieStatus.CURRENT, pageable);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(PageResponse.from(result));
    }

    @RateLimit(value = 20, duration = 1)
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming movies")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")})
    public ResponseEntity<PageResponse<MovieCardResponse>> getUpcomingMovies(
            @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/movies/upcoming - Getting upcoming movies");
        var result = movieService.getMovies(null, MovieStatus.UPCOMING, pageable);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(PageResponse.from(result));
    }

    @RateLimit(value = 20, duration = 1)
    @GetMapping("/current/home")
    @Operation(summary = "Get current movies for home page")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")})
    public ResponseEntity<List<MovieCardResponse>> getCurrentMoviesForHome() {
        log.info("GET /api/movies/current/home - Getting current movies for home page");
        var pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "releaseDate"));
        var movies = movieService.getCurrentMovies(pageable);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)).body(movies);
    }

    @RateLimit(value = 20, duration = 1)
    @GetMapping("/upcoming/home")
    @Operation(summary = "Get upcoming movies for home page")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")})
    public ResponseEntity<List<MovieCardResponse>> getUpcomingMoviesForHome() {
        log.info("GET /api/movies/upcoming/home - Getting upcoming movies for home page");
        var pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "releaseDate"));
        var movies = movieService.getUpcomingMovies(pageable);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)).body(movies);
    }

    @RateLimit(value = 20, duration = 1)
    @GetMapping("/leaving-soon/home")
    @Operation(summary = "Get leaving soon movies for home page")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")})
    public ResponseEntity<List<MovieCardResponse>> getLeavingSoonMoviesForHome() {
        log.info("GET /api/movies/leaving-soon/home - Getting leaving soon movies for home page");
        var pageable = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "endShowingDate"));
        var movies = movieService.getLeavingSoonMovies(pageable);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES)).body(movies);
    }

    @RateLimit(value = 50, duration = 1)
    @GetMapping("/{id}/poster")
    @Operation(summary = "Get movie poster")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Poster retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Movie or poster not found")})
    public ResponseEntity<byte[]> getPoster(@PathVariable Long id) {
        log.info("GET /api/movies/{}/poster - Getting movie poster", id);
        return movieService.getPoster(id);
    }

    @RateLimit(value = 20, duration = 1)
    @GetMapping("/search")
    @Operation(summary = "Search movies for session creation")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")})
    public List<MovieSessionSearchResponse> searchMovies(
            @Parameter(description = "Search query (movie title)") @RequestParam(required = false) String query,
            @Parameter(description = "Session date to filter available movies") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("GET /api/movies/search - query: '{}', date: {}", query, date);
        return movieService.searchMovies(query, date);
    }
}