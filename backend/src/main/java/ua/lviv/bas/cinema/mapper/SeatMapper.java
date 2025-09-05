package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.SeatCreateDto;
import ua.lviv.bas.cinema.dto.SeatDto;

@Mapper(componentModel = "spring")
public interface SeatMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "hall", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	Seat toEntity(SeatCreateDto dto);

	@Mapping(target = "available", ignore = true)
	SeatDto toDto(Seat entity);
}