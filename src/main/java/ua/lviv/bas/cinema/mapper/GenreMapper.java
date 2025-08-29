package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.GenreDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GenreMapper {

	GenreDto toDto(Genre genre);

	List<GenreDto> toDtoList(List<Genre> genres);

	Genre toEntity(GenreDto genreDto);

	void updateGenreFromDto(GenreDto genreDto, @org.mapstruct.MappingTarget Genre genre);
}