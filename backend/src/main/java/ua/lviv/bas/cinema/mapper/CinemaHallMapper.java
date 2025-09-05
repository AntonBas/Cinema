package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.dto.CinemaHallCreateDto;
import ua.lviv.bas.cinema.dto.CinemaHallResponseDto;

@Mapper(componentModel = "spring", uses = { SeatMapper.class })
public interface CinemaHallMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "seats", ignore = true)
	CinemaHall toEntity(CinemaHallCreateDto dto);

	@Mapping(target = "capacity", source = "entity", qualifiedByName = "calculateCapacity")
	@Mapping(target = "seats", source = "entity.seats")
	CinemaHallResponseDto toResponseDto(CinemaHall entity);

	@Named("calculateCapacity")
	default int calculateCapacity(CinemaHall hall) {
		return hall.getCapacity();
	}
}
