package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;

@Mapper(componentModel = "spring")
public interface SeatMapper {

	SeatResponse toDto(Seat seat);

	List<SeatResponse> toDtoList(List<Seat> seats);
}