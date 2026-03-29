package ua.lviv.bas.cinema.mapper.cinema;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = SeatMapper.class)
public interface CinemaHallMapper {

	@Named("calculateCapacity")
	default int calculateCapacity(CinemaHall hall) {
		return hall.getSeats() != null ? hall.getSeats().size() : 0;
	}

	@Mapping(target = "capacity", source = "hall", qualifiedByName = "calculateCapacity")
	CinemaHallResponse toCinemaHallResponse(CinemaHall hall);

	List<CinemaHallResponse> toCinemaHallResponseList(List<CinemaHall> halls);

	@Mapping(target = "capacity", source = "seatsCount")
	CinemaHallResponse toCinemaHallResponseFromProjection(CinemaHallProjection projection);

	List<CinemaHallResponse> toCinemaHallResponseListFromProjection(List<CinemaHallProjection> projections);

	@Mapping(target = "hallId", source = "id")
	@Mapping(target = "hallName", source = "name")
	@Mapping(target = "totalSeats", source = "hall", qualifiedByName = "calculateCapacity")
	HallLayoutResponse toHallLayoutResponse(CinemaHall hall);
}