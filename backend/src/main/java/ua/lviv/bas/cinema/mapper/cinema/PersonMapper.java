package ua.lviv.bas.cinema.mapper.cinema;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonListResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.PersonListProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface PersonMapper {

	@Mapping(target = "movieCount", constant = "0")
	PersonListResponse toPersonListResponse(Person person);

	@Mapping(target = "movieCount", source = "movieCount")
	PersonListResponse toPersonListResponse(PersonListProjection projection);

	PersonResponse toPersonResponse(Person person);

	@Mapping(target = "id", ignore = true)
	Person toPerson(PersonRequest personRequest);

	@Mapping(target = "id", ignore = true)
	Person toPerson(QuickCreatePersonRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	void updatePersonFromRequest(PersonRequest personRequest, @MappingTarget Person person);
}