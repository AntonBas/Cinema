package ua.lviv.bas.cinema.movie.person;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.movie.person.Person;
import ua.lviv.bas.cinema.movie.person.dto.PersonRequest;
import ua.lviv.bas.cinema.movie.person.dto.PersonListResponse;
import ua.lviv.bas.cinema.movie.person.dto.PersonResponse;
import ua.lviv.bas.cinema.movie.person.PersonListProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PersonMapper {

    @Mapping(target = "movieCount", source = "movieCount")
    PersonListResponse toPersonListResponse(PersonListProjection projection);

    PersonResponse toPersonResponse(Person person);

    @Mapping(target = "id", ignore = true)
    Person toEntity(PersonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(PersonRequest request, @MappingTarget Person person);
}