package ua.lviv.bas.cinema.movie.genre;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.movie.genre.Genre;
import ua.lviv.bas.cinema.movie.genre.dto.GenreRequest;
import ua.lviv.bas.cinema.movie.genre.dto.GenreListResponse;
import ua.lviv.bas.cinema.movie.genre.dto.GenreResponse;
import ua.lviv.bas.cinema.movie.genre.GenreListProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface GenreMapper {

    @Mapping(target = "movieCount", source = "movieCount")
    GenreListResponse toGenreListResponse(GenreListProjection projection);

    GenreResponse toGenreResponse(Genre genre);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movies", ignore = true)
    Genre toEntity(GenreRequest genreRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movies", ignore = true)
    void updateEntity(GenreRequest genreRequest, @MappingTarget Genre genre);
}