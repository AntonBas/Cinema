package ua.lviv.bas.cinema.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;

@Mapper(componentModel = "spring")
public interface CinemaHallMapper {
	CinemaHallResponse toDto(CinemaHall hall);

	List<CinemaHallResponse> toDtoList(List<CinemaHall> halls);
}