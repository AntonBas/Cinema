package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.projection.movie.MovieCardProjection;
import ua.lviv.bas.cinema.domain.projection.movie.MovieDetailProjection;
import ua.lviv.bas.cinema.domain.projection.movie.MovieSessionSearchProjection;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovieMapper {

	@Mapping(target = "posterUrl", expression = "java(\"/api/movies/\" + movie.getId() + \"/poster\")")
	MovieCardResponse toMovieCardResponse(Movie movie);

	@Mapping(target = "posterUrl", expression = "java(\"/api/movies/\" + projection.getId() + \"/poster\")")
	MovieCardResponse toMovieCardResponse(MovieCardProjection projection);

	@Mapping(target = "posterUrl", expression = "java(\"/api/movies/\" + movie.getId() + \"/poster\")")
	MovieDetailResponse toMovieDetailResponse(Movie movie);

	@Mapping(target = "posterUrl", expression = "java(\"/api/movies/\" + projection.getId() + \"/poster\")")
	MovieDetailResponse toMovieDetailResponse(MovieDetailProjection projection);

	MovieSessionSearchResponse toMovieSessionSearchResponse(MovieSessionSearchProjection projection);

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
}