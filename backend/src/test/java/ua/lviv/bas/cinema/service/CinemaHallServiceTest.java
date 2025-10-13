package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.CinemaHallCreateDto;
import ua.lviv.bas.cinema.dto.CinemaHallResponseDto;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;

@ExtendWith(MockitoExtension.class)
public class CinemaHallServiceTest {

	@Mock
	private CinemaHallRepository hallRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private CinemaHallMapper cinemaHallMapper;

	@InjectMocks
	private CinemaHallService cinemaHallService;

	private CinemaHallCreateDto createDto;
	private CinemaHall cinemaHall;
	private CinemaHallResponseDto responseDto;

	@BeforeEach
	void setUp() {
		createDto = CinemaHallCreateDto.builder().name("Hall 1").rows(5).seatsPerRow(10)
				.defaultSeatType(SeatType.STANDARD).build();

		cinemaHall = CinemaHall.builder().id(1L).name("Hall 1").build();

		responseDto = CinemaHallResponseDto.builder().id(1L).name("Hall 1").capacity(50).build();
	}

	@Test
	void createHall_ShouldCreateAndReturnHall() {
		when(cinemaHallMapper.toEntity(createDto)).thenReturn(cinemaHall);
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);
		when(seatRepository.saveAll(any())).thenReturn(Arrays.asList());
		when(cinemaHallMapper.toResponseDto(cinemaHall)).thenReturn(responseDto);

		CinemaHallResponseDto result = cinemaHallService.createHall(createDto);

		assertNotNull(result);
		assertEquals(responseDto, result);
		verify(hallRepository).save(cinemaHall);
		verify(seatRepository).saveAll(any());
	}

	@Test
	void createHall_WithVIPSeatType_ShouldGenerateVIPSeats() {
		createDto.setDefaultSeatType(SeatType.VIP);
		when(cinemaHallMapper.toEntity(createDto)).thenReturn(cinemaHall);
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);
		when(seatRepository.saveAll(any())).thenAnswer(invocation -> {
			List<Seat> seats = invocation.getArgument(0);
			assertEquals(50, seats.size());
			assertEquals(SeatType.VIP, seats.get(0).getSeatType());
			return seats;
		});
		when(cinemaHallMapper.toResponseDto(cinemaHall)).thenReturn(responseDto);

		cinemaHallService.createHall(createDto);

		verify(seatRepository).saveAll(any());
	}

	@Test
	void createHall_WithNullSeatType_ShouldUseStandardAsDefault() {
		createDto.setDefaultSeatType(null);
		when(cinemaHallMapper.toEntity(createDto)).thenReturn(cinemaHall);
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);
		when(seatRepository.saveAll(any())).thenAnswer(invocation -> {
			List<Seat> seats = invocation.getArgument(0);
			assertEquals(SeatType.STANDARD, seats.get(0).getSeatType());
			return seats;
		});
		when(cinemaHallMapper.toResponseDto(cinemaHall)).thenReturn(responseDto);

		cinemaHallService.createHall(createDto);

		verify(seatRepository).saveAll(any());
	}

	@Test
	void getHallById_WhenHallExists_ShouldReturnHall() {
		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(cinemaHallMapper.toResponseDto(cinemaHall)).thenReturn(responseDto);

		CinemaHallResponseDto result = cinemaHallService.getHallById(1L);

		assertNotNull(result);
		assertEquals(responseDto, result);
		verify(hallRepository).findById(1L);
	}

	@Test
	void getHallById_WhenHallNotExists_ShouldThrowException() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			cinemaHallService.getHallById(1L);
		});
		verify(hallRepository).findById(1L);
	}

	@Test
	void updateHall_WhenHallExists_ShouldUpdateAndReturnHall() {
		CinemaHallCreateDto updateDto = CinemaHallCreateDto.builder().name("Updated Hall").rows(6).seatsPerRow(8)
				.build();

		CinemaHall updatedHall = CinemaHall.builder().id(1L).name("Updated Hall").seats(new ArrayList<>()).build();

		CinemaHallResponseDto updatedResponseDto = CinemaHallResponseDto.builder().id(1L).name("Updated Hall")
				.capacity(48).build();

		cinemaHall.setSeats(new ArrayList<>());

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.save(cinemaHall)).thenReturn(updatedHall);
		when(seatRepository.saveAll(any())).thenReturn(Arrays.asList());
		when(cinemaHallMapper.toResponseDto(updatedHall)).thenReturn(updatedResponseDto);

		CinemaHallResponseDto result = cinemaHallService.updateHall(1L, updateDto);

		assertNotNull(result);
		assertEquals("Updated Hall", result.getName());
		verify(hallRepository).findById(1L);
		verify(hallRepository).save(cinemaHall);
		verify(seatRepository).deleteAll(cinemaHall.getSeats());
		verify(seatRepository).saveAll(any());
	}

	@Test
	void updateHall_WhenHallNotExists_ShouldThrowException() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			cinemaHallService.updateHall(1L, createDto);
		});
		verify(hallRepository).findById(1L);
	}

	@Test
	void deleteHall_WhenHallExists_ShouldDeleteHall() {
		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		doNothing().when(hallRepository).delete(cinemaHall);

		cinemaHallService.deleteHall(1L);

		verify(hallRepository).findById(1L);
		verify(hallRepository).delete(cinemaHall);
	}

	@Test
	void deleteHall_WhenHallNotExists_ShouldThrowException() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			cinemaHallService.deleteHall(1L);
		});
		verify(hallRepository).findById(1L);
	}

	@Test
	void getAllHalls_ShouldReturnAllHalls() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall);
		List<CinemaHallResponseDto> responseDtos = Arrays.asList(responseDto);

		when(hallRepository.findAll()).thenReturn(halls);
		when(cinemaHallMapper.toResponseDto(cinemaHall)).thenReturn(responseDto);

		List<CinemaHallResponseDto> result = cinemaHallService.getAllHalls();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(responseDtos, result);
		verify(hallRepository).findAll();
	}

	@Test
	void existsById_WhenHallExists_ShouldReturnTrue() {
		when(hallRepository.existsById(1L)).thenReturn(true);

		boolean result = cinemaHallService.existsById(1L);

		assertTrue(result);
		verify(hallRepository).existsById(1L);
	}

	@Test
	void existsById_WhenHallNotExists_ShouldReturnFalse() {
		when(hallRepository.existsById(1L)).thenReturn(false);

		boolean result = cinemaHallService.existsById(1L);

		assertFalse(result);
		verify(hallRepository).existsById(1L);
	}

	@Test
	void getEntityById_WhenHallExists_ShouldReturnEntity() {
		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));

		CinemaHall result = cinemaHallService.getEntityById(1L);

		assertNotNull(result);
		assertEquals(cinemaHall, result);
		verify(hallRepository).findById(1L);
	}

	@Test
	void getEntityById_WhenHallNotExists_ShouldThrowException() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			cinemaHallService.getEntityById(1L);
		});
		verify(hallRepository).findById(1L);
	}
}