package ua.lviv.bas.cinema.movie.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.movie.domain.Genre;
import ua.lviv.bas.cinema.movie.dto.request.GenreRequest;
import ua.lviv.bas.cinema.movie.dto.response.GenreListResponse;
import ua.lviv.bas.cinema.movie.dto.response.GenreResponse;
import ua.lviv.bas.cinema.movie.repository.projection.GenreListProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface GenreMapper {

    @Mapping(target = "movieCount", source = "movieCount")
    GenreListResponse toGenreListResponse(GenreListProjection projection);

    GenreResponse toGenreResponse(Genre genre);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movies", ignore = true)
    Genre toGenre(GenreRequest genreRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movies", ignore = true)
    void updateGenreFromRequest(GenreRequest genreRequest, @MappingTarget Genre genre);
}