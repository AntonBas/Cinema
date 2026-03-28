package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.projection.cinema.PersonProjection;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {

	PersonResponse toPersonResponse(Person person);

	@Mapping(target = "movieCount", source = "movieCount")
	PersonResponse toPersonResponse(PersonProjection projection);

	List<PersonResponse> toPersonResponseList(List<Person> persons);

	Person toPerson(PersonRequest personRequest);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updatePersonFromRequest(PersonRequest personRequest, @MappingTarget Person person);
}