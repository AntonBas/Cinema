package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.SessionDto;
import ua.lviv.bas.cinema.dto.SessionRequest;

@Mapper(componentModel = "spring")
public interface SessionMapper {

	@Mapping(target = "endTime", expression = "java(session.getEndTime())")
	@Mapping(target = "available", expression = "java(session.isAvailable())")
	SessionDto toDto(Session session);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	Session toEntity(SessionRequest request);
}