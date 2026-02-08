package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.projection.MovieCardProjection;
import ua.lviv.bas.cinema.domain.projection.MovieDetailProjection;
import ua.lviv.bas.cinema.domain.projection.MovieSessionSearchProjection;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovieMapper {

	@Mapping(target = "posterUrl", ignore = true)
	@Mapping(target = "currentlyShowing", ignore = true)
	MovieCardResponse toMovieCardResponse(MovieCardProjection projection);

	@Mapping(target = "posterUrl", ignore = true)
	@Mapping(target = "currentlyShowing", ignore = true)
	@Mapping(target = "upcoming", ignore = true)
	@Mapping(target = "archived", ignore = true)
	@Mapping(target = "active", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "actors", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	MovieDetailResponse toMovieDetailResponse(MovieDetailProjection projection);

	@Mapping(target = "genres", source = "genres")
	@Mapping(target = "actors", source = "actors")
	@Mapping(target = "directors", source = "directors")
	@Mapping(target = "screenwriters", source = "screenwriters")
	@Mapping(target = "posterUrl", ignore = true)
	@Mapping(target = "currentlyShowing", ignore = true)
	@Mapping(target = "upcoming", ignore = true)
	@Mapping(target = "archived", ignore = true)
	@Mapping(target = "active", ignore = true)
	MovieDetailResponse toMovieDetailResponse(Movie movie);

	@Mapping(target = "releaseYear", expression = "java(projection.getReleaseDate() != null ? projection.getReleaseDate().getYear() : null)")
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