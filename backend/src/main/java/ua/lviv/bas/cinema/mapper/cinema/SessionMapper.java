package ua.lviv.bas.cinema.mapper.cinema;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionScheduleProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SessionMapper {

	@Named("stringToStatus")
	default CinemaSessionStatus stringToStatus(String status) {
		return status != null ? CinemaSessionStatus.valueOf(status) : null;
	}

	@Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
	SessionAdminResponse toAdminResponse(SessionAdminProjection projection);

	@Mapping(target = "endTime", expression = "java(session.getMovie() != null ? session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()) : null)")
	@Mapping(target = "movieId", source = "movie.id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "movieDuration", source = "movie.durationMinutes")
	@Mapping(target = "hallId", source = "hall.id")
	@Mapping(target = "hallName", source = "hall.name")
	SessionAdminResponse toAdminResponse(Session session);

	SessionScheduleResponse toScheduleResponse(SessionScheduleProjection projection);

	@Mapping(target = "endTime", expression = "java(session.getMovie() != null ? session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes()) : null)")
	@Mapping(target = "movieId", source = "movie.id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "moviePosterFileName", source = "movie.posterFileName")
	@Mapping(target = "movieAgeRating", source = "movie.ageRating")
	@Mapping(target = "movieDuration", source = "movie.durationMinutes")
	@Mapping(target = "hallId", source = "hall.id")
	@Mapping(target = "hallName", source = "hall.name")
	@Mapping(target = "hallCapacity", ignore = true)
	@Mapping(target = "availableSeats", ignore = true)
	SessionScheduleResponse toScheduleResponse(Session session);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", constant = "SCHEDULED")
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "seatReservations", ignore = true)
	Session toEntity(SessionCreateRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "seatReservations", ignore = true)
	void updateEntity(@MappingTarget Session session, SessionUpdateRequest request);
}