package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.CinemaHallCreateDto;
import ua.lviv.bas.cinema.dto.CinemaHallResponseDto;
import ua.lviv.bas.cinema.dto.SeatDto;

@ExtendWith(MockitoExtension.class)
class CinemaHallMapperTest {

	@Mock
	private SeatMapper seatMapper;

	@InjectMocks
	private CinemaHallMapperImpl cinemaHallMapper;

	@Test
	void toEntity_ShouldMapBasicFieldsAndIgnoreOthers() {
		CinemaHallCreateDto dto = CinemaHallCreateDto.builder().name("Test Hall").rows(5).seatsPerRow(10).build();

		CinemaHall entity = cinemaHallMapper.toEntity(dto);

		assertNotNull(entity);
		assertEquals("Test Hall", entity.getName());
		assertNull(entity.getId());
		assertNull(entity.getSessions());
		assertNull(entity.getSeats());

		verifyNoInteractions(seatMapper);
	}

	@Test
	void toEntity_ShouldHandleNullDto() {
		CinemaHall entity = cinemaHallMapper.toEntity(null);

		assertNull(entity);
		verifyNoInteractions(seatMapper);
	}

	@Test
	void toResponseDto_ShouldMapAllFieldsIncludingCapacity() {
		CinemaHall hall = new CinemaHall();
		hall.setId(1L);
		hall.setName("Test Hall");

		Seat seat1 = new Seat();
		seat1.setId(1L);
		Seat seat2 = new Seat();
		seat2.setId(2L);
		hall.setSeats(List.of(seat1, seat2));

		SeatDto seatDto1 = SeatDto.builder().id(1L).build();
		SeatDto seatDto2 = SeatDto.builder().id(2L).build();

		when(seatMapper.toDto(seat1)).thenReturn(seatDto1);
		when(seatMapper.toDto(seat2)).thenReturn(seatDto2);

		CinemaHallResponseDto dto = cinemaHallMapper.toResponseDto(hall);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("Test Hall", dto.getName());
		assertEquals(2, dto.getCapacity());
		assertEquals(2, dto.getSeats().size());
		assertEquals(1L, dto.getSeats().get(0).getId());
		assertEquals(2L, dto.getSeats().get(1).getId());

		verify(seatMapper, times(2)).toDto(any(Seat.class));
	}

	@Test
	void toResponseDto_ShouldHandleNullSeats() {
		CinemaHall hall = new CinemaHall();
		hall.setId(1L);
		hall.setName("Test Hall");
		hall.setSeats(null);

		CinemaHallResponseDto dto = cinemaHallMapper.toResponseDto(hall);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("Test Hall", dto.getName());
		assertEquals(0, dto.getCapacity());
		assertNull(dto.getSeats());

		verifyNoInteractions(seatMapper);
	}

	@Test
	void toResponseDto_ShouldHandleEmptySeats() {
		CinemaHall hall = new CinemaHall();
		hall.setId(1L);
		hall.setName("Test Hall");
		hall.setSeats(List.of());

		CinemaHallResponseDto dto = cinemaHallMapper.toResponseDto(hall);

		assertNotNull(dto);
		assertEquals(1L, dto.getId());
		assertEquals("Test Hall", dto.getName());
		assertEquals(0, dto.getCapacity());
		assertNotNull(dto.getSeats());
		assertTrue(dto.getSeats().isEmpty());

		verifyNoInteractions(seatMapper);
	}

	@Test
	void toResponseDto_ShouldHandleNullEntity() {
		CinemaHallResponseDto dto = cinemaHallMapper.toResponseDto(null);

		assertNull(dto);
		verifyNoInteractions(seatMapper);
	}

	@Test
	void calculateCapacity_ShouldReturnSeatsSize() {
		CinemaHall hall = new CinemaHall();
		Seat seat1 = new Seat();
		Seat seat2 = new Seat();
		Seat seat3 = new Seat();
		hall.setSeats(List.of(seat1, seat2, seat3));

		int capacity = cinemaHallMapper.calculateCapacity(hall);

		assertEquals(3, capacity);
	}

	@Test
	void calculateCapacity_ShouldReturnZeroForNullSeats() {
		CinemaHall hall = new CinemaHall();
		hall.setSeats(null);

		int capacity = cinemaHallMapper.calculateCapacity(hall);

		assertEquals(0, capacity);
	}

	@Test
	void calculateCapacity_ShouldReturnZeroForEmptySeats() {
		CinemaHall hall = new CinemaHall();
		hall.setSeats(List.of());

		int capacity = cinemaHallMapper.calculateCapacity(hall);

		assertEquals(0, capacity);
	}
}