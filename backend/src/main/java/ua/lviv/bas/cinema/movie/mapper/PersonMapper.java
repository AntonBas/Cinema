package ua.lviv.bas.cinema.movie.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.movie.domain.Person;
import ua.lviv.bas.cinema.movie.dto.request.PersonRequest;
import ua.lviv.bas.cinema.movie.dto.response.PersonListResponse;
import ua.lviv.bas.cinema.movie.dto.response.PersonResponse;
import ua.lviv.bas.cinema.movie.repository.projection.PersonListProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PersonMapper {

    @Mapping(target = "movieCount", source = "movieCount")
    PersonListResponse toPersonListResponse(PersonListProjection projection);

    PersonResponse toPersonResponse(Person person);

    @Mapping(target = "id", ignore = true)
    Person toPerson(PersonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updatePersonFromRequest(PersonRequest request, @MappingTarget Person person);
}