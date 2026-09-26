package ua.lviv.bas.cinema.cinema.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.response.SeatResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatMapperTest {

    private final SeatMapper seatMapper = Mappers.getMapper(SeatMapper.class);

    @Test
    void toResponseShouldMapAllFields() {
        Seat seat = Seat.builder().id(1L).row(1).number(5).seatType(SeatType.VIP).x(120).y(60).active(true).build();

        SeatResponse response = seatMapper.toResponse(seat);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.row()).isEqualTo(1);
        assertThat(response.number()).isEqualTo(5);
        assertThat(response.seatType()).isEqualTo(SeatType.VIP);
        assertThat(response.x()).isEqualTo(120);
        assertThat(response.y()).isEqualTo(60);
        assertThat(response.active()).isTrue();
    }

    @Test
    void toResponseShouldMapInactiveSeat() {
        Seat seat = Seat.builder().id(2L).row(2).number(10).seatType(SeatType.STANDARD).active(false).build();

        SeatResponse response = seatMapper.toResponse(seat);

        assertThat(response.active()).isFalse();
    }

    @Test
    void toResponseShouldHandleNullInput() {
        SeatResponse response = seatMapper.toResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toResponseListShouldMapList() {
        List<Seat> seats = Arrays.asList(
                Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).active(true).build(),
                Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).active(false).build());

        List<SeatResponse> responses = seatMapper.toResponseList(seats);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(0).active()).isTrue();
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(1).active()).isFalse();
    }

    @Test
    void toResponseListShouldReturnEmptyListForEmptyInput() {
        List<SeatResponse> responses = seatMapper.toResponseList(Collections.emptyList());
        assertThat(responses).isEmpty();
    }

    @Test
    void toResponseListShouldReturnNullForNullInput() {
        List<SeatResponse> responses = seatMapper.toResponseList(null);
        assertThat(responses).isNull();
    }
}