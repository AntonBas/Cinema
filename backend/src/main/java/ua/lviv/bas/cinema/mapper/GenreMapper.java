package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.GenreDto;
import ua.lviv.bas.cinema.dto.movie.GenreRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GenreMapper {

	GenreDto toDto(Genre genre);

	List<GenreDto> toDtoList(List<Genre> genres);

	Genre toEntity(GenreRequest genreRequest);

	void updateGenreFromRequest(GenreRequest genreRequest, @MappingTarget Genre genre);
}