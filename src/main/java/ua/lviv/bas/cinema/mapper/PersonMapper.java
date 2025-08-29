package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.dto.PersonDTO;

@Mapper(componentModel = "spring")
public interface PersonMapper {
    PersonDTO toDto(Person person);
    List<PersonDTO> toDtoList(List<Person> persons);
    Person toEntity(PersonDTO personDTO);
    void updateEntityFromDto(PersonDTO personDTO, @MappingTarget Person person);
}