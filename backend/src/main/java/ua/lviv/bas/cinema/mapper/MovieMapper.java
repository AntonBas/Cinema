package ua.lviv.bas.cinema.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovieMapper {

	@Mapping(target = "castIds", source = "cast", qualifiedByName = "mapPersonsToIds")
	@Mapping(target = "directorIds", source = "directors", qualifiedByName = "mapPersonsToIds")
	@Mapping(target = "screenwriterIds", source = "screenwriters", qualifiedByName = "mapPersonsToIds")
	@Mapping(target = "genreIds", source = "genres", qualifiedByName = "mapGenresToIds")
	@Mapping(target = "trailerUrl", source = "trailerUrl")
	MovieDto toDto(Movie movie);

	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "cast", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "posterFileName", ignore = true)
	@Mapping(target = "trailerUrl", source = "trailerUrl")
	Movie toEntity(MovieDto movieDto);

	default Movie toEntity(MovieCreateRequest request) {
		Movie movie = toEntity(request.getMovie());
		return movie;
	}

	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "cast", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "posterFileName", ignore = true)
	@Mapping(target = "trailerUrl", source = "trailerUrl")
	void updateMovieFromDto(MovieDto movieDto, @MappingTarget Movie movie);

	@Named("mapPersonsToIds")
	default List<Long> mapPersonsToIds(Set<ua.lviv.bas.cinema.domain.Person> persons) {
		if (persons == null)
			return List.of();
		return persons.stream().map(ua.lviv.bas.cinema.domain.Person::getId).collect(Collectors.toList());
	}

	@Named("mapGenresToIds")
	default List<Long> mapGenresToIds(Set<ua.lviv.bas.cinema.domain.Genre> genres) {
		if (genres == null)
			return List.of();
		return genres.stream().map(ua.lviv.bas.cinema.domain.Genre::getId).collect(Collectors.toList());
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "cast", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	@Mapping(target = "posterFileName", ignore = true)
	@Mapping(target = "trailerUrl", source = "trailerUrl")
	void updateBasicFields(MovieDto movieDto, @MappingTarget Movie movie);
}