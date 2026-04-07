package ua.lviv.bas.cinema.mapper.cinema;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieAdminResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieCardProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieSessionSearchProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { PersonMapper.class,
		GenreMapper.class, SessionMapper.class })
public interface MovieMapper {

	@Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
	MovieCardResponse toMovieCardResponse(Movie movie);

	@Mapping(target = "posterUrl", expression = "java(getPosterUrl(projection.getId()))")
	MovieCardResponse toMovieCardResponse(MovieCardProjection projection);

	@Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
	@Mapping(target = "genres", source = "genres")
	@Mapping(target = "actors", source = "actors")
	@Mapping(target = "directors", source = "directors")
	@Mapping(target = "screenwriters", source = "screenwriters")
	@Mapping(target = "sessions", source = "sessions")
	MovieDetailResponse toMovieDetailResponse(Movie movie);

	@Mapping(target = "posterUrl", expression = "java(getPosterUrl(movie.getId()))")
	@Mapping(target = "genres", source = "genres")
	@Mapping(target = "actors", source = "actors")
	@Mapping(target = "directors", source = "directors")
	@Mapping(target = "screenwriters", source = "screenwriters")
	MovieAdminResponse toMovieAdminResponse(Movie movie);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "title", source = "title")
	@Mapping(target = "durationMinutes", source = "durationMinutes")
	MovieSessionSearchResponse toMovieSessionSearchResponse(MovieSessionSearchProjection projection);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "title", source = "title")
	@Mapping(target = "durationMinutes", source = "durationMinutes")
	MovieSessionSearchResponse toMovieSessionSearchResponse(MovieCardProjection projection);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "actors", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "posterFileName", ignore = true)
	Movie toMovie(MovieCreateRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "actors", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "posterFileName", ignore = true)
	void updateMovieFromRequest(MovieUpdateRequest request, @MappingTarget Movie movie);

	default String getPosterUrl(Long id) {
		return id != null ? "/api/movies/" + id + "/poster" : null;
	}
}