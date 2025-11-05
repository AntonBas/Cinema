package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.shared.SeatDto;

@Mapper(componentModel = "spring")
public interface SeatMapper {

	SeatDto toDto(Seat seat);

	List<SeatDto> toDtoList(List<Seat> seats);
}