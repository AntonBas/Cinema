package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CinemaHallMapper {

	@Mapping(target = "capacity", expression = "java(hall.getSeats() != null ? hall.getSeats().size() : 0)")
	CinemaHallResponse toCinemaHallResponse(CinemaHall hall);

	List<CinemaHallResponse> toCinemaHallResponseList(List<CinemaHall> halls);
}