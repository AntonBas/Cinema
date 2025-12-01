package ua.lviv.bas.cinema.mapper;

import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;

@Mapper(componentModel = "spring")
public interface PersonMapper {

	PersonResponse toDto(Person person);

	List<PersonResponse> toDtoList(Set<Person> persons);

	List<PersonResponse> toDtoList(List<Person> persons);

	@Mapping(target = "id", ignore = true)
	Person toEntity(PersonRequest personRequest);

	@Mapping(target = "id", ignore = true)
	void updatePersonFromRequest(PersonRequest personRequest, @MappingTarget Person person);

	@Mapping(target = "id", ignore = true)
	Person toEntity(QuickCreatePersonRequest quickCreateDto);

	PersonRequest toPersonRequest(QuickCreatePersonRequest quickCreateDto);
}