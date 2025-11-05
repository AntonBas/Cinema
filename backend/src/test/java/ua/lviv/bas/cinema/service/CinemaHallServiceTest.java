package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.CinemaHallDto;
import ua.lviv.bas.cinema.dto.cinemaHall.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.cinemaHall.CinemaHallWithSeatsDto;
import ua.lviv.bas.cinema.dto.cinemaHall.SeatLayoutRequest;
import ua.lviv.bas.cinema.exception.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.exception.DuplicateEntityException;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;

@ExtendWith(MockitoExtension.class)
class CinemaHallServiceTest {

	@Mock
	private CinemaHallRepository hallRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private CinemaHallMapper hallMapper;

	@Mock
	private SeatMapper seatMapper;

	@InjectMocks
	private CinemaHallService cinemaHallService;

	private CinemaHall cinemaHall;
	private CinemaHallRequest hallRequest;
	private CinemaHallDto hallDto;
	private SeatLayoutRequest seatLayoutRequest;

	@BeforeEach
	void setUp() {
		cinemaHall = CinemaHall.builder().id(1L).name("Hall A").build();

		hallRequest = CinemaHallRequest.builder().name("Hall A").build();

		hallDto = CinemaHallDto.builder().id(1L).name("Hall A").capacity(0).build();

		seatLayoutRequest = SeatLayoutRequest.builder().rows(5).seatsPerRow(10).defaultSeatType(SeatType.STANDARD)
				.build();
	}

	@Test
	void createHall_ShouldCreateAndReturnHallDto() {
		when(hallRepository.existsByName("Hall A")).thenReturn(false);
		when(hallRepository.save(any(CinemaHall.class))).thenReturn(cinemaHall);
		when(hallMapper.toDto(cinemaHall)).thenReturn(hallDto);

		CinemaHallDto result = cinemaHallService.createHall(hallRequest);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		verify(hallRepository).existsByName("Hall A");
		verify(hallRepository).save(any(CinemaHall.class));
		verify(hallMapper).toDto(cinemaHall);
	}

	@Test
	void createHall_ShouldThrowDuplicateEntityException_WhenNameExists() {
		when(hallRepository.existsByName("Hall A")).thenReturn(true);

		assertThrows(DuplicateEntityException.class, () -> cinemaHallService.createHall(hallRequest));
		verify(hallRepository).existsByName("Hall A");
		verify(hallRepository, never()).save(any());
	}

	@Test
	void getHallById_ShouldReturnHallDto_WhenHallExists() {
		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallMapper.toDto(cinemaHall)).thenReturn(hallDto);

		CinemaHallDto result = cinemaHallService.getHallById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		verify(hallRepository).findById(1L);
		verify(hallMapper).toDto(cinemaHall);
	}

	@Test
	void getHallById_ShouldThrowCinemaHallNotFoundException_WhenHallNotExists() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallService.getHallById(1L));
		verify(hallRepository).findById(1L);
		verify(hallMapper, never()).toDto(any());
	}

	@Test
	void updateHall_ShouldUpdateAndReturnHallDto_WhenHallExists() {
		CinemaHallRequest updateRequest = CinemaHallRequest.builder().name("Updated Hall").build();
		CinemaHallDto updatedDto = CinemaHallDto.builder().id(1L).name("Updated Hall").capacity(0).build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.existsByName("Updated Hall")).thenReturn(false);
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);
		when(hallMapper.toDto(cinemaHall)).thenReturn(updatedDto);

		CinemaHallDto result = cinemaHallService.updateHall(1L, updateRequest);

		assertNotNull(result);
		assertEquals("Updated Hall", result.getName());
		verify(hallRepository).findById(1L);
		verify(hallRepository).existsByName("Updated Hall");
		verify(hallRepository).save(cinemaHall);
		verify(hallMapper).toDto(cinemaHall);
	}

	@Test
	void updateHall_ShouldThrowEntityNotFoundException_WhenHallNotExists() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> cinemaHallService.updateHall(1L, hallRequest));
		verify(hallRepository).findById(1L);
		verify(hallRepository, never()).save(any());
	}

	@Test
	void updateHall_ShouldThrowDuplicateEntityException_WhenNewNameExists() {
		CinemaHallRequest updateRequest = CinemaHallRequest.builder().name("Existing Hall").build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.existsByName("Existing Hall")).thenReturn(true);

		assertThrows(DuplicateEntityException.class, () -> cinemaHallService.updateHall(1L, updateRequest));
		verify(hallRepository).findById(1L);
		verify(hallRepository).existsByName("Existing Hall");
		verify(hallRepository, never()).save(any());
	}

	@Test
	void deleteHall_ShouldDeleteHall_WhenHallExists() {
		when(hallRepository.existsById(1L)).thenReturn(true);

		cinemaHallService.deleteHall(1L);

		verify(hallRepository).existsById(1L);
		verify(hallRepository).deleteById(1L);
	}

	@Test
	void deleteHall_ShouldThrowCinemaHallNotFoundException_WhenHallNotExists() {
		when(hallRepository.existsById(1L)).thenReturn(false);

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallService.deleteHall(1L));
		verify(hallRepository).existsById(1L);
		verify(hallRepository, never()).deleteById(any());
	}

	@Test
	void getAllHalls_ShouldReturnListOfHallDtos() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall);
		List<CinemaHallDto> hallDtos = Arrays.asList(hallDto);

		when(hallRepository.findAll()).thenReturn(halls);
		when(hallMapper.toDtoList(halls)).thenReturn(hallDtos);

		List<CinemaHallDto> result = cinemaHallService.getAllHalls();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(hallRepository).findAll();
		verify(hallMapper).toDtoList(halls);
	}

	@Test
	void generateSeats_ShouldGenerateSeatsAndReturnHallWithSeatsDto() {
		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);

		CinemaHallWithSeatsDto result = cinemaHallService.generateSeats(1L, seatLayoutRequest);

		assertNotNull(result);
		verify(hallRepository).findById(1L);
		verify(seatRepository).deleteByHallId(1L);
		verify(hallRepository).save(cinemaHall);
	}

	@Test
	void getHallWithSeats_ShouldReturnHallWithSeatsDto_WhenHallExists() {
		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.of(cinemaHall));

		CinemaHallWithSeatsDto result = cinemaHallService.getHallWithSeats(1L);

		assertNotNull(result);
		verify(hallRepository).findByIdWithSeats(1L);
	}

	@Test
	void searchHalls_ShouldReturnFilteredHalls_WhenNameProvided() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall);
		List<CinemaHallDto> hallDtos = Arrays.asList(hallDto);

		when(hallRepository.findByNameContainingIgnoreCase("Hall")).thenReturn(halls);
		when(hallMapper.toDtoList(halls)).thenReturn(hallDtos);

		List<CinemaHallDto> result = cinemaHallService.searchHalls("Hall");

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(hallRepository).findByNameContainingIgnoreCase("Hall");
		verify(hallMapper).toDtoList(halls);
	}

	@Test
	void searchHalls_ShouldReturnAllHalls_WhenNameIsEmpty() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall);
		List<CinemaHallDto> hallDtos = Arrays.asList(hallDto);

		when(hallRepository.findAll()).thenReturn(halls);
		when(hallMapper.toDtoList(halls)).thenReturn(hallDtos);

		List<CinemaHallDto> result = cinemaHallService.searchHalls("");

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(hallRepository).findAll();
		verify(hallMapper).toDtoList(halls);
	}

	@Test
	void getHallLayout_ShouldThrowEntityNotFoundException_WhenHallNotExists() {
		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> cinemaHallService.getHallLayout(1L));
		verify(hallRepository).findByIdWithSeats(1L);
	}
}