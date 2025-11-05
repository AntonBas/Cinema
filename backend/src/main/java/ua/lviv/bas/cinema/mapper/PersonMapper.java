package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.dto.movie.PersonDto;
import ua.lviv.bas.cinema.dto.movie.PersonRequest;
import ua.lviv.bas.cinema.dto.shared.QuickCreatePersonDto;

@Mapper(componentModel = "spring")
public interface PersonMapper {

	PersonDto toDto(Person person);

	List<PersonDto> toDtoList(List<Person> persons);

	@Mapping(target = "id", ignore = true)
	Person toEntity(PersonRequest personRequest);

	@Mapping(target = "id", ignore = true)
	void updatePersonFromRequest(PersonRequest personRequest, @MappingTarget Person person);

	@Mapping(target = "id", ignore = true)
	Person toEntity(QuickCreatePersonDto quickCreateDto);

	PersonRequest toPersonRequest(QuickCreatePersonDto quickCreateDto);
}