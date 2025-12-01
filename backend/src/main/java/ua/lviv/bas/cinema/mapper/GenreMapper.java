package ua.lviv.bas.cinema.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GenreMapper {

	GenreResponse toDto(Genre genre);

	List<GenreResponse> toDtoList(Set<Genre> genres);

	List<GenreResponse> toDtoList(List<Genre> genres);

	Genre toEntity(GenreRequest genreRequest);

	void updateGenreFromRequest(GenreRequest genreRequest, @MappingTarget Genre genre);
}