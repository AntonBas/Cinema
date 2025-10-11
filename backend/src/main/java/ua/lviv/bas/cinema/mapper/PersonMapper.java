package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.dto.PersonDto;
import ua.lviv.bas.cinema.dto.QuickCreatePersonDto;

@Mapper(componentModel = "spring")
public interface PersonMapper {

	PersonDto toDto(Person person);

	List<PersonDto> toDtoList(List<Person> persons);

	Person toEntity(PersonDto personDto);

	void updateEntityFromDto(PersonDto personDto, @MappingTarget Person person);

	@Mapping(target = "id", ignore = true)
	Person toEntity(QuickCreatePersonDto quickCreateDto);

}