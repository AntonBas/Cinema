package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;

@Mapper(componentModel = "spring")
public interface SessionMapper {

	@Mapping(target = "movieId", source = "movie.id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "movieDuration", source = "movie.durationMinutes")
	@Mapping(target = "hallId", source = "hall.id")
	@Mapping(target = "hallName", source = "hall.name")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "endTime", ignore = true)
	@Mapping(target = "hallCapacity", ignore = true)
	@Mapping(target = "ticketsSold", ignore = true)
	@Mapping(target = "totalRevenue", ignore = true)
	SessionAdminResponse toAdminDto(Session session);

	@Mapping(target = "movieId", source = "movie.id")
	@Mapping(target = "movieTitle", source = "movie.title")
	@Mapping(target = "moviePosterFileName", source = "movie.posterFileName")
	@Mapping(target = "movieAgeRating", source = "movie.ageRating")
	@Mapping(target = "movieDuration", source = "movie.durationMinutes")
	@Mapping(target = "hallId", source = "hall.id")
	@Mapping(target = "hallName", source = "hall.name")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "endTime", ignore = true)
	@Mapping(target = "availableSeats", ignore = true)
	@Mapping(target = "hallCapacity", ignore = true)
	SessionScheduleResponse toScheduleDto(Session session);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "status", constant = "SCHEDULED")
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "bookedSeats", ignore = true)
	Session toEntity(SessionCreateRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "movie", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "bookedSeats", ignore = true)
	void updateEntityFromDto(SessionUpdateRequest request, @MappingTarget Session session);
}