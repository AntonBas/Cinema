package ua.lviv.bas.cinema.cinema.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.dto.hall.request.SeatLayoutItemRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.response.SeatResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface SeatMapper {

    SeatResponse toResponse(Seat seat);

    List<SeatResponse> toResponseList(List<Seat> seats);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "row", source = "item.row")
    @Mapping(target = "number", source = "item.number")
    @Mapping(target = "seatType", source = "item.seatType")
    @Mapping(target = "x", source = "item.x")
    @Mapping(target = "y", source = "item.y")
    @Mapping(target = "active", source = "item.active")
    @Mapping(target = "hall", source = "hall")
    @Mapping(target = "seatReservations", ignore = true)
    Seat toEntity(SeatLayoutItemRequest item, CinemaHall hall);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hall", ignore = true)
    @Mapping(target = "seatReservations", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    void updateEntity(SeatLayoutItemRequest item, @MappingTarget Seat seat);
}