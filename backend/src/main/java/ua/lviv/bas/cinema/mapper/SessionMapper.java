package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

	@Mapping(target = "movieId", source = "movie.id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "movieDuration", source = "movie.durationMinutes")
	@Mapping(target = "hallId", source = "hall.id")
	@Mapping(target = "hallName", source = "hall.name")
	@Mapping(target = "endTime", ignore = true)
	@Mapping(target = "hallCapacity", ignore = true)
	@Mapping(target = "ticketsSold", ignore = true)
	@Mapping(target = "totalRevenue", ignore = true)
	SessionAdminResponse toSessionAdminResponse(Session session);

	@Mapping(target = "movieId", source = "movie.id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "moviePosterFileName", source = "movie.posterFileName")
	@Mapping(target = "movieAgeRating", source = "movie.ageRating")
	@Mapping(target = "movieDuration", source = "movie.durationMinutes")
	@Mapping(target = "hallId", source = "hall.id")
	@Mapping(target = "hallName", source = "hall.name")
	@Mapping(target = "endTime", ignore = true)
	@Mapping(target = "availableSeats", ignore = true)
	@Mapping(target = "hallCapacity", ignore = true)
	SessionScheduleResponse toSessionScheduleResponse(Session session);

	@Mapping(target = "movieTitle", source = "movieTitle")
	@Mapping(target = "movieDuration", source = "movieDuration")
	@Mapping(target = "hallName", source = "hallName")
	@Mapping(target = "hallCapacity", source = "hallCapacity")
	@Mapping(target = "ticketsSold", source = "ticketsSold")
	@Mapping(target = "totalRevenue", source = "totalRevenue")
	@Mapping(target = "endTime", source = "endTime")
	SessionAdminResponse toSessionAdminResponse(SessionAdminProjection projection);

	@Mapping(target = "movieTitle", source = "movieTitle")
	@Mapping(target = "moviePosterFileName", source = "moviePosterFileName")
	@Mapping(target = "movieAgeRating", source = "movieAgeRating")
	@Mapping(target = "movieDuration", source = "movieDuration")
	@Mapping(target = "hallName", source = "hallName")
	@Mapping(target = "hallCapacity", source = "hallCapacity")
	@Mapping(target = "availableSeats", ignore = true)
	@Mapping(target = "endTime", source = "endTime")
	@Mapping(target = "status", source = "status", qualifiedByName = "stringToCinemaSessionStatus")
	SessionScheduleResponse toSessionScheduleResponse(SessionScheduleProjection projection);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", constant = "SCHEDULED")
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "bookedSeats", ignore = true)
	Session toSession(SessionCreateRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "bookedSeats", ignore = true)
	void updateSessionFromRequest(SessionUpdateRequest request, @MappingTarget Session session);

	@Named("stringToCinemaSessionStatus")
	default CinemaSessionStatus stringToCinemaSessionStatus(String status) {
		return status != null ? CinemaSessionStatus.valueOf(status) : CinemaSessionStatus.SCHEDULED;
	}
}