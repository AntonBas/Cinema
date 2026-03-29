package ua.lviv.bas.cinema.mapper.cinema;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.GenreProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GenreMapper {

	GenreResponse toGenreResponse(Genre genre);

	@Mapping(target = "movieCount", source = "movieCount")
	GenreResponse toGenreResponse(GenreProjection projection);

	List<GenreResponse> toGenreResponseList(List<Genre> genres);

	Genre toGenre(GenreRequest genreRequest);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateGenreFromRequest(GenreRequest genreRequest, @MappingTarget Genre genre);
}