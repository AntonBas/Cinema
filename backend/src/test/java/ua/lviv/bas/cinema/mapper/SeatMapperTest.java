package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.shared.SeatDto;

class SeatMapperTest {

	private SeatMapper seatMapper;
	private Seat standardSeat;
	private Seat vipSeat;

	@BeforeEach
	void setUp() {
		seatMapper = Mappers.getMapper(SeatMapper.class);

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();

		standardSeat = Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).hall(hall).build();

		vipSeat = Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).hall(hall).build();
	}

	@Test
	void testToDto_ShouldMapSeatToDto() {
		SeatDto dto = seatMapper.toDto(standardSeat);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals(1, dto.getRow());
		assertEquals(1, dto.getNumber());
		assertEquals(SeatType.STANDARD, dto.getSeatType());
	}

	@Test
	void testToDto_WithVipSeat_ShouldMapCorrectly() {
		SeatDto dto = seatMapper.toDto(vipSeat);

		assertNotNull(dto);
		assertEquals(2L, dto.getId());
		assertEquals(1, dto.getRow());
		assertEquals(2, dto.getNumber());
		assertEquals(SeatType.VIP, dto.getSeatType());
	}

	@Test
	void testToDtoList_ShouldMapListOfSeats() {
		List<Seat> seats = Arrays.asList(standardSeat, vipSeat);

		List<SeatDto> dtos = seatMapper.toDtoList(seats);

		assertNotNull(dtos);
		assertEquals(2, dtos.size());
		assertEquals(1L, dtos.get(0).getId());
		assertEquals(2L, dtos.get(1).getId());
		assertEquals(SeatType.STANDARD, dtos.get(0).getSeatType());
		assertEquals(SeatType.VIP, dtos.get(1).getSeatType());
	}

	@Test
	void testToDtoList_WithEmptyList_ShouldReturnEmptyList() {
		List<Seat> seats = Arrays.asList();

		List<SeatDto> dtos = seatMapper.toDtoList(seats);

		assertNotNull(dtos);
		assertTrue(dtos.isEmpty());
	}

	@Test
	void testToDtoList_WithNullList_ShouldReturnNull() {
		List<SeatDto> dtos = seatMapper.toDtoList(null);

		assertNull(dtos);
	}
}