package ua.lviv.bas.cinema.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.dto.MovieDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovieMapper {

	@Mapping(target = "sessionIds", source = "sessions", qualifiedByName = "mapSessionsToIds")
	@Mapping(target = "castIds", source = "cast", qualifiedByName = "mapPersonsToIds")
	@Mapping(target = "directorIds", source = "directors", qualifiedByName = "mapPersonsToIds")
	@Mapping(target = "screenwriterIds", source = "screenwriters", qualifiedByName = "mapPersonsToIds")
	@Mapping(target = "genreIds", source = "genres", qualifiedByName = "mapGenresToIds")
	@Mapping(target = "releaseYear", source = "releaseDate", qualifiedByName = "mapReleaseYear")
	@Mapping(target = "isCurrentlyShowing", expression = "java(movie.isCurrentlyShowing())")
	@Mapping(target = "isUpcoming", expression = "java(movie.isUpcoming())")
	MovieDto toDto(Movie movie);

	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "cast", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	Movie toEntity(MovieDto movieDto);

	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "cast", ignore = true)
	@Mapping(target = "directors", ignore = true)
	@Mapping(target = "screenwriters", ignore = true)
	@Mapping(target = "genres", ignore = true)
	void updateMovieFromDto(MovieDto movieDto, @MappingTarget Movie movie);

	@Named("mapSessionsToIds")
	default Set<Long> mapSessionsToIds(Set<ua.lviv.bas.cinema.domain.Session> sessions) {
		if (sessions == null)
			return Set.of();
		return sessions.stream().map(ua.lviv.bas.cinema.domain.Session::getId).collect(Collectors.toSet());
	}

	@Named("mapPersonsToIds")
	default Set<Long> mapPersonsToIds(Set<ua.lviv.bas.cinema.domain.Person> persons) {
		if (persons == null)
			return Set.of();
		return persons.stream().map(ua.lviv.bas.cinema.domain.Person::getId).collect(Collectors.toSet());
	}

	@Named("mapGenresToIds")
	default Set<Long> mapGenresToIds(Set<ua.lviv.bas.cinema.domain.Genre> genres) {
		if (genres == null)
			return Set.of();
		return genres.stream().map(ua.lviv.bas.cinema.domain.Genre::getId).collect(Collectors.toSet());
	}

	@Named("mapReleaseYear")
	default Integer mapReleaseYear(java.time.LocalDate releaseDate) {
		return releaseDate != null ? releaseDate.getYear() : null;
	}
}