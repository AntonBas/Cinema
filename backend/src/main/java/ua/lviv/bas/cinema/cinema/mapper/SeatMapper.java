package ua.lviv.bas.cinema.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.dto.hall.response.SeatResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface SeatMapper {

	SeatResponse toSeatResponse(Seat seat);

	List<SeatResponse> toSeatResponseList(List<Seat> seats);
}