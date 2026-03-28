package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.response.SeatResponse;

public class SeatMapperTest {

	private SeatMapper seatMapper = Mappers.getMapper(SeatMapper.class);

	@Test
	void toSeatResponseShouldMapAllFields() {
		Seat seat = Seat.builder().id(1L).row(1).number(5).seatType(SeatType.VIP).active(true).build();

		SeatResponse response = seatMapper.toSeatResponse(seat);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.row()).isEqualTo(1);
		assertThat(response.number()).isEqualTo(5);
		assertThat(response.seatType()).isEqualTo(SeatType.VIP);
		assertThat(response.active()).isTrue();
	}

	@Test
	void toSeatResponseShouldMapInactiveSeat() {
		Seat seat = Seat.builder().id(2L).row(2).number(10).seatType(SeatType.STANDARD).active(false).build();

		SeatResponse response = seatMapper.toSeatResponse(seat);

		assertThat(response.active()).isFalse();
	}

	@Test
	void toSeatResponseShouldHandleNullInput() {
		SeatResponse response = seatMapper.toSeatResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toSeatResponseListShouldMapList() {
		List<Seat> seats = Arrays.asList(
				Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).active(true).build(),
				Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).active(false).build());

		List<SeatResponse> responses = seatMapper.toSeatResponseList(seats);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).id()).isEqualTo(1L);
		assertThat(responses.get(0).active()).isTrue();
		assertThat(responses.get(1).id()).isEqualTo(2L);
		assertThat(responses.get(1).active()).isFalse();
	}

	@Test
	void toSeatResponseListShouldReturnEmptyListForEmptyInput() {
		List<SeatResponse> responses = seatMapper.toSeatResponseList(Collections.emptyList());
		assertThat(responses).isEmpty();
	}

	@Test
	void toSeatResponseListShouldReturnNullForNullInput() {
		List<SeatResponse> responses = seatMapper.toSeatResponseList(null);
		assertThat(responses).isNull();
	}
}