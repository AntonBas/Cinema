package ua.lviv.bas.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.dto.hall.response.SeatResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatMapper {

	SeatResponse toSeatResponse(Seat seat);

	List<SeatResponse> toSeatResponseList(List<Seat> seats);
}