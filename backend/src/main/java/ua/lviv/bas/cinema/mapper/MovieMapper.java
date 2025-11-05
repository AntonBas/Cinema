package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.MovieDto;
import ua.lviv.bas.cinema.dto.movie.MovieResponse;
import ua.lviv.bas.cinema.dto.movie.MovieUpdateRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovieMapper {

	@Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie))")
	@Mapping(target = "currentlyShowing", expression = "java(isCurrentlyShowing(movie))")
	@Mapping(target = "upcoming", expression = "java(isUpcoming(movie))")
	@Mapping(target = "archived", expression = "java(isArchived(movie))")
	@Mapping(target = "active", expression = "java(isActive(movie))")
	MovieDto toDto(Movie movie);

	List<MovieDto> toDtoList(List<Movie> movies);

	@Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie))")
	@Mapping(target = "currentlyShowing", expression = "java(isCurrentlyShowing(movie))")
	MovieResponse toResponse(Movie movie);

	List<MovieResponse> toResponseList(List<Movie> movies);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "actors", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "posterFileName", ignore = true)
	Movie toEntity(MovieCreateRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "actors", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "posterFileName", ignore = true)
	void updateEntityFromRequest(MovieUpdateRequest request, @MappingTarget Movie movie);

	default String getPosterUrl(Movie movie) {
		if (movie.getPosterFileName() != null && !movie.getPosterFileName().isBlank()) {
			return "/api/movies/" + movie.getId() + "/poster";
		}
		return "/images/default-poster.jpg";
	}

	default boolean isCurrentlyShowing(Movie movie) {
		return movie.getStatus() == MovieStatus.CURRENT;
	}

	default boolean isUpcoming(Movie movie) {
		return movie.getStatus() == MovieStatus.UPCOMING;
	}

	default boolean isArchived(Movie movie) {
		return movie.getStatus() == MovieStatus.ARCHIVED;
	}

	default boolean isActive(Movie movie) {
		return movie.getStatus() != MovieStatus.ARCHIVED;
	}
}