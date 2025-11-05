package ua.lviv.bas.cinema.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.cinemaHall.CinemaHallDto;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CinemaHallMapperTest {

	private CinemaHallMapper cinemaHallMapper;
	private CinemaHall cinemaHallWithSeats;
	private CinemaHall cinemaHallEmptySeats;
	private CinemaHall cinemaHallNullSeats;

	@BeforeEach
	void setUp() {
		cinemaHallMapper = Mappers.getMapper(CinemaHallMapper.class);

		cinemaHallWithSeats = CinemaHall.builder().id(1L).name("Hall A").build();

		Seat seat1 = Seat.builder().id(1L).row(1).number(1).hall(cinemaHallWithSeats).build();
		Seat seat2 = Seat.builder().id(2L).row(1).number(2).hall(cinemaHallWithSeats).build();
		Seat seat3 = Seat.builder().id(3L).row(2).number(1).hall(cinemaHallWithSeats).build();
		cinemaHallWithSeats.setSeats(Arrays.asList(seat1, seat2, seat3));

		cinemaHallEmptySeats = CinemaHall.builder().id(2L).name("Hall B").build();
		cinemaHallEmptySeats.setSeats(Arrays.asList());

		cinemaHallNullSeats = CinemaHall.builder().id(3L).name("Hall C").build();
		cinemaHallNullSeats.setSeats(null);
	}

	@Test
	void testToDto_ShouldMapCinemaHallToDto() {
		CinemaHallDto dto = cinemaHallMapper.toDto(cinemaHallWithSeats);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("Hall A", dto.getName());
		assertEquals(3, dto.getCapacity());
	}

	@Test
	void testToDto_WithEmptySeats_ShouldReturnZeroCapacity() {
		CinemaHallDto dto = cinemaHallMapper.toDto(cinemaHallEmptySeats);

		assertNotNull(dto);
		assertEquals(2L, dto.getId());
		assertEquals("Hall B", dto.getName());
		assertEquals(0, dto.getCapacity());
	}

	@Test
	void testToDto_WithNullSeats_ShouldReturnZeroCapacity() {
		CinemaHallDto dto = cinemaHallMapper.toDto(cinemaHallNullSeats);

		assertNotNull(dto);
		assertEquals(3L, dto.getId());
		assertEquals("Hall C", dto.getName());
		assertEquals(0, dto.getCapacity());
	}

	@Test
	void testToDtoList_ShouldMapListOfCinemaHalls() {
		List<CinemaHall> halls = Arrays.asList(cinemaHallWithSeats, cinemaHallEmptySeats);

		List<CinemaHallDto> dtos = cinemaHallMapper.toDtoList(halls);

		assertNotNull(dtos);
		assertEquals(2, dtos.size());
		assertEquals(1L, dtos.get(0).getId());
		assertEquals(2L, dtos.get(1).getId());
		assertEquals(3, dtos.get(0).getCapacity());
		assertEquals(0, dtos.get(1).getCapacity());
	}

	@Test
	void testToDtoList_WithEmptyList_ShouldReturnEmptyList() {
		List<CinemaHall> halls = Arrays.asList();

		List<CinemaHallDto> dtos = cinemaHallMapper.toDtoList(halls);

		assertNotNull(dtos);
		assertTrue(dtos.isEmpty());
	}

	@Test
	void testToDtoList_WithNullList_ShouldReturnNull() {
		List<CinemaHallDto> dtos = cinemaHallMapper.toDtoList(null);

		assertNull(dtos);
	}
}