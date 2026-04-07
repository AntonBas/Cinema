package ua.lviv.bas.cinema.mapper.cinema;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreInfoResponse;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.GenreProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface GenreMapper {

	@Mapping(target = "movieCount", expression = "java(genre.getMovies() != null ? genre.getMovies().size() : 0)")
	GenreResponse toGenreResponse(Genre genre);

	@Mapping(target = "movieCount", source = "movieCount")
	GenreResponse toGenreResponse(GenreProjection projection);

	GenreInfoResponse toGenreInfoResponse(Genre genre);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movies", ignore = true)
	Genre toGenre(GenreRequest genreRequest);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movies", ignore = true)
	void updateGenreFromRequest(GenreRequest genreRequest, @MappingTarget Genre genre);
}