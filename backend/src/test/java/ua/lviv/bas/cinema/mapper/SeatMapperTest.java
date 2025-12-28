package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;

class SeatMapperTest {

	private SeatMapper seatMapper;
	private Seat activeStandardSeat;
	private Seat inactiveVipSeat;
	private Seat activeCoupleSeat;

	@BeforeEach
	void setUp() {
		seatMapper = Mappers.getMapper(SeatMapper.class);

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();

		activeStandardSeat = Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).active(true).hall(hall)
				.build();

		inactiveVipSeat = Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).active(false).hall(hall)
				.build();

		activeCoupleSeat = Seat.builder().id(3L).row(2).number(1).seatType(SeatType.COUPLE).active(true).hall(hall)
				.build();
	}

	@Test
	void toDto_ShouldMapAllFieldsIncludingActive() {
		SeatResponse dto = seatMapper.toDto(activeStandardSeat);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getRow()).isEqualTo(1);
		assertThat(dto.getNumber()).isEqualTo(1);
		assertThat(dto.getSeatType()).isEqualTo(SeatType.STANDARD);
		assertThat(dto.isActive()).isTrue();
	}

	@Test
	void toDto_ShouldMapInactiveSeat() {
		SeatResponse dto = seatMapper.toDto(inactiveVipSeat);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(2L);
		assertThat(dto.getSeatType()).isEqualTo(SeatType.VIP);
		assertThat(dto.isActive()).isFalse();
	}

	@Test
	void toDto_ShouldMapActiveSeatByDefaultWhenNotSpecified() {
		Seat seat = Seat.builder().id(4L).row(3).number(3).seatType(SeatType.STANDARD).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto.isActive()).isTrue();
	}

	@Test
	void toDto_ShouldMapSeatWithoutBuilder() {
		Seat seat = new Seat();
		seat.setId(6L);
		seat.setRow(4);
		seat.setNumber(5);
		seat.setSeatType(SeatType.STANDARD);
		seat.setActive(true);

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto)
				.extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber,
						SeatResponse::getSeatType, SeatResponse::isActive)
				.containsExactly(6L, 4, 5, SeatType.STANDARD, true);
	}

	@Test
	void toDto_ShouldReturnNull_WhenInputIsNull() {
		SeatResponse dto = seatMapper.toDto(null);

		assertThat(dto).isNull();
	}

	@ParameterizedTest
	@EnumSource(SeatType.class)
	void toDto_ShouldMapAllSeatTypes(SeatType seatType) {
		Seat seat = Seat.builder().id(7L).row(5).number(6).seatType(seatType).active(true).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto.getSeatType()).isEqualTo(seatType);
	}

	@Test
	void toDto_ShouldMapSeatWithNullHall() {
		Seat seat = Seat.builder().id(8L).row(6).number(7).seatType(SeatType.VIP).active(true).hall(null).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto)
				.extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber,
						SeatResponse::getSeatType, SeatResponse::isActive)
				.containsExactly(8L, 6, 7, SeatType.VIP, true);
	}

	@ParameterizedTest
	@CsvSource({ "0, 0, true", "0, 0, false", "999, 999, true", "999, 999, false" })
	void toDto_ShouldHandleVariousRowNumberAndActiveCombinations(int row, int number, boolean active) {
		Seat seat = Seat.builder().id(9L).row(row).number(number).seatType(SeatType.STANDARD).active(active).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto.getRow()).isEqualTo(row);
		assertThat(dto.getNumber()).isEqualTo(number);
		assertThat(dto.isActive()).isEqualTo(active);
	}

	@Test
	void toDtoList_ShouldMapListOfSeats() {
		List<Seat> seats = Arrays.asList(activeStandardSeat, inactiveVipSeat, activeCoupleSeat);

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).isNotNull().hasSize(3);
		assertThat(dtos.get(0).getId()).isEqualTo(1L);
		assertThat(dtos.get(1).getId()).isEqualTo(2L);
		assertThat(dtos.get(2).getId()).isEqualTo(3L);
	}

	@Test
	void toDtoList_ShouldMapSeatsInCorrectOrder() {
		List<Seat> seats = Arrays.asList(
				Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).active(true).build(),
				Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).active(true).build(),
				Seat.builder().id(3L).row(2).number(1).seatType(SeatType.COUPLE).active(false).build());

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).extracting(SeatResponse::getRow, SeatResponse::getNumber, SeatResponse::isActive)
				.containsExactly(tuple(1, 1, true), tuple(1, 2, true), tuple(2, 1, false));
	}

	@Test
	void toDtoList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<SeatResponse> dtos = seatMapper.toDtoList(Collections.emptyList());

		assertThat(dtos).isNotNull().isEmpty();
	}

	@Test
	void toDtoList_ShouldReturnNull_WhenInputIsNull() {
		List<SeatResponse> dtos = seatMapper.toDtoList(null);

		assertThat(dtos).isNull();
	}

	@Test
	void toDtoList_ShouldHandleListWithNullElements() {
		List<Seat> seats = Arrays.asList(activeStandardSeat, null, inactiveVipSeat, null, activeCoupleSeat);

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).hasSize(5);
		assertThat(dtos.get(0)).isNotNull();
		assertThat(dtos.get(1)).isNull();
		assertThat(dtos.get(2)).isNotNull();
		assertThat(dtos.get(3)).isNull();
		assertThat(dtos.get(4)).isNotNull();
	}

	@Test
	void toDtoList_ShouldHandleSingleElementList() {
		List<Seat> seats = Collections.singletonList(activeStandardSeat);

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).hasSize(1).first().extracting(SeatResponse::getId).isEqualTo(1L);
	}

	@Test
	void toDtoList_ShouldPreserveSeatTypeAndActiveStatus() {
		List<Seat> seats = Arrays.asList(activeStandardSeat, inactiveVipSeat, activeCoupleSeat);

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).extracting(SeatResponse::getSeatType, SeatResponse::isActive).containsExactly(
				tuple(SeatType.STANDARD, true), tuple(SeatType.VIP, false), tuple(SeatType.COUPLE, true));
	}

	@Test
	void toDto_ShouldMapSeatWithAllSeatTypesAndActiveStatus() {
		for (SeatType seatType : SeatType.values()) {
			for (boolean active : new boolean[] { true, false }) {
				Seat seat = Seat.builder().id(10L).row(7).number(8).seatType(seatType).active(active).build();

				SeatResponse dto = seatMapper.toDto(seat);

				assertThat(dto.getSeatType()).isEqualTo(seatType);
				assertThat(dto.isActive()).isEqualTo(active);
			}
		}
	}

	@Test
	void consistencyCheck_ShouldMapCorrectlyForAllFields() {
		Seat seat = Seat.builder().id(11L).row(10).number(20).seatType(SeatType.COUPLE).active(false).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto)
				.extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber,
						SeatResponse::getSeatType, SeatResponse::isActive)
				.containsExactly(11L, 10, 20, SeatType.COUPLE, false);
	}

	@Test
	void toDtoList_ShouldMapSeatsWithMixedActiveStatus() {
		List<Seat> seats = Arrays.asList(
				Seat.builder().id(12L).row(1).number(1).seatType(SeatType.STANDARD).active(true).build(),
				Seat.builder().id(13L).row(1).number(2).seatType(SeatType.VIP).active(false).build(),
				Seat.builder().id(14L).row(2).number(1).seatType(SeatType.COUPLE).active(true).build(),
				Seat.builder().id(15L).row(2).number(2).seatType(SeatType.STANDARD).active(false).build());

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).extracting(SeatResponse::isActive).containsExactly(true, false, true, false);
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void toDto_ShouldCorrectlyMapActiveStatus(boolean active) {
		Seat seat = Seat.builder().id(16L).row(8).number(9).seatType(SeatType.VIP).active(active).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto.isActive()).isEqualTo(active);
	}
}