package ua.lviv.bas.cinema.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.dto.cinemaHall.CinemaHallDto;

@Mapper(componentModel = "spring")
public interface CinemaHallMapper {
	CinemaHallDto toDto(CinemaHall hall);

	List<CinemaHallDto> toDtoList(List<CinemaHall> halls);
}