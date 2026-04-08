package ua.lviv.bas.cinema.mapper.cinema;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.hall.response.SeatRowResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallListProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, uses = SeatMapper.class)
public abstract class CinemaHallMapper {

	@Autowired
	protected SeatMapper seatMapper;

	@Named("calculateCapacity")
	protected int calculateCapacity(CinemaHall hall) {
		return hall.getSeats() != null ? hall.getSeats().size() : 0;
	}

	@Mapping(target = "capacity", source = "hall", qualifiedByName = "calculateCapacity")
	public abstract CinemaHallListResponse toCinemaHallListResponse(CinemaHall hall);

	public abstract List<CinemaHallListResponse> toCinemaHallListResponseList(List<CinemaHall> halls);

	@Mapping(target = "capacity", source = "seatsCount")
	public abstract CinemaHallListResponse toCinemaHallListResponse(CinemaHallListProjection projection);

	@Mapping(target = "rows", ignore = true)
	@Mapping(target = "seatsPerRow", ignore = true)
	@Mapping(target = "defaultSeatType", ignore = true)
	@Mapping(target = "coupleRows", ignore = true)
	@Mapping(target = "capacity", source = "hall", qualifiedByName = "calculateCapacity")
	public abstract CinemaHallResponse toCinemaHallResponse(CinemaHall hall);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sessions", ignore = true)
	@Mapping(target = "seats", ignore = true)
	public abstract CinemaHall toEntity(CinemaHallRequest request);

	@Mapping(target = "hallId", source = "id")
	@Mapping(target = "hallName", source = "name")
	@Mapping(target = "totalRows", expression = "java(hall.getSeats().stream().mapToInt(Seat::getRow).max().orElse(0))")
	@Mapping(target = "maxSeatsPerRow", expression = "java(hall.getSeats().stream().collect(Collectors.groupingBy(Seat::getRow, Collectors.counting())).values().stream().mapToInt(Long::intValue).max().orElse(0))")
	@Mapping(target = "totalSeats", source = "hall", qualifiedByName = "calculateCapacity")
	@Mapping(target = "rows", source = "seats")
	public abstract HallLayoutResponse toHallLayoutResponse(CinemaHall hall);

	protected List<SeatRowResponse> mapSeatsToRows(List<Seat> seats) {
		return seats.stream().collect(Collectors.groupingBy(Seat::getRow)).entrySet().stream()
				.map(entry -> new SeatRowResponse(entry.getKey(), entry.getValue().size(),
						seatMapper.toSeatResponseList(entry.getValue())))
				.sorted((a, b) -> a.rowNumber() - b.rowNumber()).toList();
	}
}