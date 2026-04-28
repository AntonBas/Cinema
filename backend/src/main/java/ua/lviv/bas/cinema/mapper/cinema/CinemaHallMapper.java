package ua.lviv.bas.cinema.mapper.cinema;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.hall.response.SeatRowResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallListProjection;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, uses = SeatMapper.class)
public abstract class CinemaHallMapper {

    @Autowired
    protected SeatMapper seatMapper;

    @Named("calculateCapacity")
    protected int calculateCapacity(CinemaHall hall) {
        return hall.getSeats() != null ? hall.getSeats().size() : 0;
    }

    @Named("calculateTotalRows")
    protected int calculateTotalRows(CinemaHall hall) {
        if (hall.getSeats() == null || hall.getSeats().isEmpty()) {
            return 0;
        }
        return hall.getSeats().stream().mapToInt(Seat::getRow).max().orElse(0);
    }

    @Named("calculateMaxSeatsPerRow")
    protected int calculateMaxSeatsPerRow(CinemaHall hall) {
        if (hall.getSeats() == null || hall.getSeats().isEmpty()) {
            return 0;
        }
        return hall.getSeats().stream().collect(Collectors.groupingBy(Seat::getRow, Collectors.counting())).values()
                .stream().mapToInt(Long::intValue).max().orElse(0);
    }

    @Named("calculateDefaultSeatType")
    protected SeatType calculateDefaultSeatType(CinemaHall hall) {
        if (hall.getSeats() == null || hall.getSeats().isEmpty()) {
            return null;
        }
        return hall.getSeats().stream().filter(s -> s.getSeatType() != SeatType.COUPLE).findFirst()
                .map(Seat::getSeatType).orElse(null);
    }

    @Named("calculateCoupleRows")
    protected List<Integer> calculateCoupleRows(CinemaHall hall) {
        if (hall.getSeats() == null || hall.getSeats().isEmpty()) {
            return List.of();
        }
        return hall.getSeats().stream().filter(s -> s.getSeatType() == SeatType.COUPLE).map(Seat::getRow).distinct()
                .sorted().toList();
    }

    @Mapping(target = "capacity", source = "hall", qualifiedByName = "calculateCapacity")
    public abstract CinemaHallListResponse toCinemaHallListResponse(CinemaHall hall);

    @Mapping(target = "capacity", source = "seatsCount")
    public abstract CinemaHallListResponse toCinemaHallListResponse(CinemaHallListProjection projection);

    @Mapping(target = "rows", source = "hall", qualifiedByName = "calculateTotalRows")
    @Mapping(target = "seatsPerRow", source = "hall", qualifiedByName = "calculateMaxSeatsPerRow")
    @Mapping(target = "defaultSeatType", source = "hall", qualifiedByName = "calculateDefaultSeatType")
    @Mapping(target = "coupleRows", source = "hall", qualifiedByName = "calculateCoupleRows")
    @Mapping(target = "capacity", source = "hall", qualifiedByName = "calculateCapacity")
    public abstract CinemaHallResponse toCinemaHallResponse(CinemaHall hall);

    @Mapping(target = "hallId", source = "id")
    @Mapping(target = "hallName", source = "name")
    @Mapping(target = "totalRows", source = "hall", qualifiedByName = "calculateTotalRows")
    @Mapping(target = "maxSeatsPerRow", source = "hall", qualifiedByName = "calculateMaxSeatsPerRow")
    @Mapping(target = "totalSeats", source = "hall", qualifiedByName = "calculateCapacity")
    @Mapping(target = "rows", source = "seats")
    public abstract HallLayoutResponse toHallLayoutResponse(CinemaHall hall);

    protected List<SeatRowResponse> mapSeatsToRows(List<Seat> seats) {
        if (seats == null) {
            return List.of();
        }
        return seats.stream().collect(Collectors.groupingBy(Seat::getRow)).entrySet().stream()
                .map(entry -> new SeatRowResponse(entry.getKey(), entry.getValue().size(),
                        seatMapper.toSeatResponseList(entry.getValue())))
                .sorted(Comparator.comparingInt(SeatRowResponse::rowNumber)).toList();
    }
}