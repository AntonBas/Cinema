package ua.lviv.bas.cinema.mapper.cinema;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionMovieInfoResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionScheduleProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface SessionMapper {

	@Named("stringToStatus")
	default CinemaSessionStatus stringToStatus(String status) {
		return status != null ? CinemaSessionStatus.valueOf(status) : null;
	}

	@Mapping(target = "endTime", expression = "java(session.getMovie() != null ? session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()) : null)")
	@Mapping(target = "movieId", source = "movie.id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "movieDuration", source = "movie.durationMinutes")
	@Mapping(target = "hallId", source = "hall.id")
	@Mapping(target = "hallName", source = "hall.name")
	SessionResponse toSessionResponse(Session session);

	@Mapping(target = "endTime", expression = "java(projection.getStartTime().plusMinutes(projection.getMovieDuration()))")
	@Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
	@Mapping(target = "hallCapacity", source = "hallCapacity")
	@Mapping(target = "ticketsSold", source = "ticketsSold")
	@Mapping(target = "totalRevenue", source = "totalRevenue")
	SessionAdminResponse toSessionAdminResponse(SessionAdminProjection projection);

	@Mapping(target = "endTime", expression = "java(projection.getStartTime().plusMinutes(projection.getMovieDuration()))")
	@Mapping(target = "withAvailableSeats", ignore = true)
	SessionScheduleResponse toSessionScheduleResponse(SessionScheduleProjection projection);

	@Mapping(target = "hallName", source = "hall.name")
	@Mapping(target = "availableSeats", ignore = true)
	SessionMovieInfoResponse toSessionMovieInfoResponse(Session session);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", constant = "SCHEDULED")
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "seatReservations", ignore = true)
	Session toEntity(SessionRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "seatReservations", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	void updateEntity(SessionRequest request, @MappingTarget Session session);
}