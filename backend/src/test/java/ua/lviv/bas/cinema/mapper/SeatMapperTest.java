package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;

class SeatMapperTest {

	private SeatMapper seatMapper;
	private Seat standardSeat;
	private Seat vipSeat;
	private Seat disabledSeat;
	private Seat loveSeat;

	@BeforeEach
	void setUp() {
		seatMapper = Mappers.getMapper(SeatMapper.class);

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();

		standardSeat = Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).hall(hall).build();

		vipSeat = Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).hall(hall).build();

		disabledSeat = Seat.builder().id(3L).row(2).number(1).seatType(SeatType.DISABLED).hall(hall).build();

		loveSeat = Seat.builder().id(4L).row(2).number(2).seatType(SeatType.COUPLE).hall(hall).build();
	}

	@Test
	void toDto_ShouldMapAllFields() {
		SeatResponse dto = seatMapper.toDto(standardSeat);

		assertThat(dto).isNotNull().extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber,
				SeatResponse::getSeatType).containsExactly(1L, 1, 1, SeatType.STANDARD);
	}

	@Test
	void toDto_ShouldMapVipSeat() {
		SeatResponse dto = seatMapper.toDto(vipSeat);

		assertThat(dto).extracting(SeatResponse::getId, SeatResponse::getSeatType).containsExactly(2L, SeatType.VIP);
	}

	@Test
	void toDto_ShouldMapDisabledSeat() {
		SeatResponse dto = seatMapper.toDto(disabledSeat);

		assertThat(dto).extracting(SeatResponse::getId, SeatResponse::getSeatType).containsExactly(3L,
				SeatType.DISABLED);
	}

	@Test
	void toDto_ShouldMapLoveSeat() {
		SeatResponse dto = seatMapper.toDto(loveSeat);

		assertThat(dto).extracting(SeatResponse::getId, SeatResponse::getSeatType).containsExactly(4L, SeatType.COUPLE);
	}

	@ParameterizedTest
	@EnumSource(SeatType.class)
	void toDto_ShouldMapAllSeatTypes(SeatType seatType) {
		Seat seat = Seat.builder().id(5L).row(3).number(3).seatType(seatType).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto.getSeatType()).isEqualTo(seatType);
	}

	@Test
	void toDto_ShouldReturnNull_WhenInputIsNull() {
		SeatResponse dto = seatMapper.toDto(null);

		assertThat(dto).isNull();
	}

	@Test
	void toDto_ShouldMapSeatWithoutBuilder() {
		Seat seat = new Seat();
		seat.setId(6L);
		seat.setRow(4);
		seat.setNumber(5);
		seat.setSeatType(SeatType.STANDARD);

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto).extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber,
				SeatResponse::getSeatType).containsExactly(6L, 4, 5, SeatType.STANDARD);
	}

	@Test
	void toDto_ShouldMapSeatWithNullHall() {
		Seat seat = Seat.builder().id(7L).row(5).number(6).seatType(SeatType.VIP).hall(null).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto).extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber)
				.containsExactly(7L, 5, 6);
	}

	@Test
	void toDto_ShouldHandleZeroRowAndNumber() {
		Seat seat = Seat.builder().id(8L).row(0).number(0).seatType(SeatType.STANDARD).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto.getRow()).isZero();
		assertThat(dto.getNumber()).isZero();
	}

	@Test
	void toDto_ShouldHandleLargeRowAndNumber() {
		Seat seat = Seat.builder().id(9L).row(999).number(999).seatType(SeatType.VIP).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto.getRow()).isEqualTo(999);
		assertThat(dto.getNumber()).isEqualTo(999);
	}

	@Test
	void toDtoList_ShouldMapListOfSeats() {
		List<Seat> seats = Arrays.asList(standardSeat, vipSeat, disabledSeat, loveSeat);

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).isNotNull().hasSize(4).extracting(SeatResponse::getId).containsExactly(1L, 2L, 3L, 4L);
	}

	@Test
	void toDtoList_ShouldMapSeatsInCorrectOrder() {
		List<Seat> seats = Arrays.asList(Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).build(),
				Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).build(),
				Seat.builder().id(3L).row(2).number(1).seatType(SeatType.DISABLED).build());

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).extracting(SeatResponse::getRow, SeatResponse::getNumber).containsExactly(tuple(1, 1),
				tuple(1, 2), tuple(2, 1));
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
		List<Seat> seats = Arrays.asList(standardSeat, null, vipSeat, null, disabledSeat);

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
		List<Seat> seats = Collections.singletonList(standardSeat);

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).hasSize(1).first().extracting(SeatResponse::getId).isEqualTo(1L);
	}

	@Test
	void toDto_ShouldMapSeatWithAllSeatTypes() {
		for (SeatType seatType : SeatType.values()) {
			Seat seat = Seat.builder().id(10L).row(1).number(1).seatType(seatType).build();

			SeatResponse dto = seatMapper.toDto(seat);

			assertThat(dto.getSeatType()).isEqualTo(seatType);
		}
	}

	@Test
	void toDto_ShouldNotIncludeHallInformation() {
		SeatResponse dto = seatMapper.toDto(standardSeat);

		assertThat(dto).extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber,
				SeatResponse::getSeatType).containsExactly(1L, 1, 1, SeatType.STANDARD);
	}

	@Test
	void consistencyCheck_ShouldMapCorrectlyForAllFields() {
		Seat seat = Seat.builder().id(11L).row(10).number(20).seatType(SeatType.COUPLE).build();

		SeatResponse dto = seatMapper.toDto(seat);

		assertThat(dto).extracting(SeatResponse::getId, SeatResponse::getRow, SeatResponse::getNumber,
				SeatResponse::getSeatType).containsExactly(11L, 10, 20, SeatType.COUPLE);
	}

	@Test
	void toDtoList_ShouldPreserveSeatTypeDistribution() {
		List<Seat> seats = Arrays.asList(standardSeat, vipSeat, disabledSeat, loveSeat);

		List<SeatResponse> dtos = seatMapper.toDtoList(seats);

		assertThat(dtos).extracting(SeatResponse::getSeatType).containsExactly(SeatType.STANDARD, SeatType.VIP,
				SeatType.DISABLED, SeatType.COUPLE);
	}
}