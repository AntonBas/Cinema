package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

	@Mapping(target = "endTime", expression = "java(session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()))")
	@Mapping(target = "ticketsSold", ignore = true)
	@Mapping(target = "totalRevenue", ignore = true)
	@Mapping(target = "hallCapacity", ignore = true)
	SessionAdminResponse toSessionAdminResponse(Session session);

	SessionAdminResponse toSessionAdminResponse(SessionAdminProjection projection);

	@Mapping(target = "endTime", expression = "java(session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()))")
	@Mapping(target = "availableSeats", ignore = true)
	@Mapping(target = "hallCapacity", ignore = true)
	SessionScheduleResponse toSessionScheduleResponse(Session session);

	SessionScheduleResponse toSessionScheduleResponse(SessionScheduleProjection projection);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", constant = "SCHEDULED")
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "seatReservations", ignore = true)
	Session toSession(SessionCreateRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "seatReservations", ignore = true)
	void updateSessionFromRequest(SessionUpdateRequest request, @MappingTarget Session session);
}