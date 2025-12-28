package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;

@Mapper(componentModel = "spring")
public interface CinemaHallMapper {

	@Mapping(target = "capacity", expression = "java(hall.getSeats() != null ? hall.getSeats().size() : 0)")
	CinemaHallResponse toDto(CinemaHall hall);

	List<CinemaHallResponse> toDtoList(List<CinemaHall> halls);
}