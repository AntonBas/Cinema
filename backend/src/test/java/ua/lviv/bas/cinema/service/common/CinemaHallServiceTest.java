package ua.lviv.bas.cinema.service.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
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
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallWithSeatsResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallNotFoundException;
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
	private CinemaHallResponse hallDto;
	private List<Seat> seats;

	@BeforeEach
	void setUp() {
		cinemaHall = CinemaHall.builder().id(1L).name("Hall A").seats(new ArrayList<>()).build();

		hallRequest = CinemaHallRequest.builder().name("Hall A").build();

		hallDto = CinemaHallResponse.builder().id(1L).name("Hall A").capacity(0).build();

		seats = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			seats.add(
					Seat.builder().id((long) i).row(1).number(i).seatType(SeatType.STANDARD).hall(cinemaHall).build());
		}
		cinemaHall.setSeats(seats);
	}

	@Test
	void createHall_ShouldCreateAndReturnHallDto_WithoutSeats() {
		when(hallRepository.existsByName("Hall A")).thenReturn(false);
		when(hallRepository.save(any(CinemaHall.class))).thenReturn(cinemaHall);
		when(hallMapper.toDto(cinemaHall)).thenReturn(hallDto);

		CinemaHallResponse result = cinemaHallService.createHall(hallRequest);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		verify(hallRepository).existsByName("Hall A");
		verify(hallRepository).save(any(CinemaHall.class));
		verify(hallMapper).toDto(cinemaHall);
	}

	@Test
	void createHall_ShouldCreateHallWithSeats_WhenSeatConfigProvided() {
		CinemaHallRequest requestWithSeats = CinemaHallRequest.builder().name("Hall B").rows(5).seatsPerRow(10)
				.defaultSeatType(SeatType.STANDARD).build();

		CinemaHall hallWithSeats = CinemaHall.builder().id(2L).name("Hall B").seats(new ArrayList<>()).build();

		CinemaHallResponse hallDtoWithCapacity = CinemaHallResponse.builder().id(2L).name("Hall B").capacity(50)
				.build();

		when(hallRepository.existsByName("Hall B")).thenReturn(false);
		when(hallRepository.save(any(CinemaHall.class))).thenReturn(hallWithSeats);
		when(hallMapper.toDto(hallWithSeats)).thenReturn(hallDtoWithCapacity);

		CinemaHallResponse result = cinemaHallService.createHall(requestWithSeats);

		assertNotNull(result);
		assertEquals(2L, result.getId());
		assertEquals("Hall B", result.getName());
		assertEquals(50, result.getCapacity());
		verify(hallRepository).existsByName("Hall B");
		verify(hallRepository).save(any(CinemaHall.class));
		verify(hallMapper).toDto(hallWithSeats);
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

		CinemaHallResponse result = cinemaHallService.getHallById(1L);

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
	void updateHall_ShouldUpdateNameAndReturnHallDto_WhenHallExists() {
		CinemaHallRequest updateRequest = CinemaHallRequest.builder().name("Updated Hall").build();
		CinemaHallResponse updatedDto = CinemaHallResponse.builder().id(1L).name("Updated Hall").capacity(0).build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.existsByName("Updated Hall")).thenReturn(false);
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);
		when(hallMapper.toDto(cinemaHall)).thenReturn(updatedDto);

		CinemaHallResponse result = cinemaHallService.updateHall(1L, updateRequest);

		assertNotNull(result);
		assertEquals("Updated Hall", result.getName());
		verify(hallRepository).findById(1L);
		verify(hallRepository).existsByName("Updated Hall");
		verify(hallRepository).save(cinemaHall);
		verify(hallMapper).toDto(cinemaHall);
	}

	@Test
	void updateHall_ShouldUpdateSeats_WhenSeatConfigProvided() {
		CinemaHall hallWithSeats = CinemaHall.builder().id(1L).name("Hall A").seats(new ArrayList<>()).build();

		CinemaHallRequest updateRequest = CinemaHallRequest.builder().name("Updated Hall").rows(6).seatsPerRow(8)
				.defaultSeatType(SeatType.VIP).build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(hallWithSeats));
		when(hallRepository.existsByName("Updated Hall")).thenReturn(false);
		when(seatRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
		when(hallRepository.save(hallWithSeats)).thenReturn(hallWithSeats);
		when(hallMapper.toDto(hallWithSeats)).thenReturn(hallDto);

		CinemaHallResponse result = cinemaHallService.updateHall(1L, updateRequest);

		assertNotNull(result);
		verify(hallRepository).findById(1L);
		verify(seatRepository).deleteByHallId(1L);
		verify(seatRepository).saveAll(anyList());
		verify(hallRepository).save(hallWithSeats);
	}

	@Test
	void updateHall_ShouldNotUpdateSeats_WhenSeatConfigNotProvided() {
		CinemaHallRequest updateRequest = CinemaHallRequest.builder().name("Updated Hall").build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.existsByName("Updated Hall")).thenReturn(false);
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);
		when(hallMapper.toDto(cinemaHall)).thenReturn(hallDto);

		CinemaHallResponse result = cinemaHallService.updateHall(1L, updateRequest);

		assertNotNull(result);
		verify(hallRepository).findById(1L);
		verify(seatRepository, never()).deleteByHallId(any());
		verify(seatRepository, never()).saveAll(anyList());
		verify(hallRepository).save(cinemaHall);
	}

	@Test
	void updateHall_ShouldThrowCinemaHallNotFoundException_WhenHallNotExists() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallService.updateHall(1L, hallRequest));
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
		verify(seatRepository, never()).deleteByHallId(any());
		verify(seatRepository, never()).saveAll(anyList());
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
		List<CinemaHallResponse> hallDtos = Arrays.asList(hallDto);

		when(hallRepository.findAll()).thenReturn(halls);
		when(hallMapper.toDtoList(halls)).thenReturn(hallDtos);

		List<CinemaHallResponse> result = cinemaHallService.getAllHalls();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(hallRepository).findAll();
		verify(hallMapper).toDtoList(halls);
	}

	@Test
	void getHallWithSeats_ShouldReturnHallWithSeatsDto_WhenHallExists() {
		CinemaHall hallWithSeats = CinemaHall.builder().id(1L).name("Hall A").seats(seats).build();

		List<SeatResponse> seatResponses = Arrays.asList(SeatResponse.builder().id(1L).build(),
				SeatResponse.builder().id(2L).build(), SeatResponse.builder().id(3L).build());

		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.of(hallWithSeats));
		when(seatMapper.toDtoList(seats)).thenReturn(seatResponses);

		CinemaHallWithSeatsResponse result = cinemaHallService.getHallWithSeats(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Hall A", result.getName());
		assertEquals(3, result.getCapacity());
		assertEquals(3, result.getSeats().size());
		verify(hallRepository).findByIdWithSeats(1L);
		verify(seatMapper).toDtoList(seats);
	}

	@Test
	void getHallWithSeats_ShouldThrowCinemaHallNotFoundException_WhenHallNotExists() {
		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.empty());

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallService.getHallWithSeats(1L));
		verify(hallRepository).findByIdWithSeats(1L);
	}

	@Test
	void getHallLayout_ShouldReturnHallLayout_WhenHallExists() {
		CinemaHall hallWithSeats = CinemaHall.builder().id(1L).name("Hall A").seats(seats).build();

		List<SeatResponse> seatResponses = Arrays.asList(SeatResponse.builder().id(1L).build(),
				SeatResponse.builder().id(2L).build(), SeatResponse.builder().id(3L).build());

		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.of(hallWithSeats));
		when(seatMapper.toDtoList(anyList())).thenReturn(seatResponses);

		HallLayoutResponse result = cinemaHallService.getHallLayout(1L);

		assertNotNull(result);
		assertEquals(1L, result.getHallId());
		assertEquals("Hall A", result.getHallName());
		assertEquals(3, result.getTotalSeats());
		verify(hallRepository).findByIdWithSeats(1L);
		verify(seatMapper).toDtoList(anyList());
	}

	@Test
	void getHallLayout_ShouldThrowCinemaHallNotFoundException_WhenHallNotExists() {
		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.empty());

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallService.getHallLayout(1L));
		verify(hallRepository).findByIdWithSeats(1L);
	}

	@Test
	void searchHalls_ShouldReturnFilteredHalls_WhenNameProvided() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall);
		List<CinemaHallResponse> hallDtos = Arrays.asList(hallDto);

		when(hallRepository.findByNameContainingIgnoreCase("Hall")).thenReturn(halls);
		when(hallMapper.toDtoList(halls)).thenReturn(hallDtos);

		List<CinemaHallResponse> result = cinemaHallService.searchHalls("Hall");

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(hallRepository).findByNameContainingIgnoreCase("Hall");
		verify(hallMapper).toDtoList(halls);
	}

	@Test
	void searchHalls_ShouldReturnAllHalls_WhenNameIsEmpty() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall);
		List<CinemaHallResponse> hallDtos = Arrays.asList(hallDto);

		when(hallRepository.findAll()).thenReturn(halls);
		when(hallMapper.toDtoList(halls)).thenReturn(hallDtos);

		List<CinemaHallResponse> result = cinemaHallService.searchHalls("");

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(hallRepository).findAll();
		verify(hallMapper).toDtoList(halls);
	}

	@Test
	void searchHalls_ShouldReturnAllHalls_WhenNameIsNull() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall);
		List<CinemaHallResponse> hallDtos = Arrays.asList(hallDto);

		when(hallRepository.findAll()).thenReturn(halls);
		when(hallMapper.toDtoList(halls)).thenReturn(hallDtos);

		List<CinemaHallResponse> result = cinemaHallService.searchHalls(null);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(hallRepository).findAll();
		verify(hallMapper).toDtoList(halls);
	}

	@Test
	void getHallEntityById_ShouldReturnHallEntity_WhenHallExists() {
		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));

		CinemaHall result = cinemaHallService.getHallEntityById(1L);

		assertNotNull(result);
		assertEquals(cinemaHall, result);
		verify(hallRepository).findById(1L);
	}

	@Test
	void getHallEntityById_ShouldThrowCinemaHallNotFoundException_WhenHallNotExists() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(CinemaHallNotFoundException.class, () -> cinemaHallService.getHallEntityById(1L));
		verify(hallRepository).findById(1L);
	}

	@Test
	void getAllHalls_WhenNoHalls_ShouldReturnEmptyList() {
		when(hallRepository.findAll()).thenReturn(new ArrayList<>());
		when(hallMapper.toDtoList(anyList())).thenReturn(new ArrayList<>());

		List<CinemaHallResponse> result = cinemaHallService.getAllHalls();

		assertNotNull(result);
		assertEquals(0, result.size());
		verify(hallRepository).findAll();
		verify(hallMapper).toDtoList(anyList());
	}

	@Test
	void searchHalls_WhenNoMatchingHalls_ShouldReturnEmptyList() {
		when(hallRepository.findByNameContainingIgnoreCase("Nonexistent")).thenReturn(new ArrayList<>());
		when(hallMapper.toDtoList(anyList())).thenReturn(new ArrayList<>());

		List<CinemaHallResponse> result = cinemaHallService.searchHalls("Nonexistent");

		assertNotNull(result);
		assertEquals(0, result.size());
		verify(hallRepository).findByNameContainingIgnoreCase("Nonexistent");
		verify(hallMapper).toDtoList(anyList());
	}

	@Test
	void determineSeatType_ShouldReturnVIP_ForLastTwoRows() {
		CinemaHallRequest request = CinemaHallRequest.builder().rows(10).defaultSeatType(SeatType.STANDARD).build();

		assertEquals(SeatType.VIP,
				ReflectionTestUtils.invokeMethod(cinemaHallService, "determineSeatType", request, 9, 1));
		assertEquals(SeatType.VIP,
				ReflectionTestUtils.invokeMethod(cinemaHallService, "determineSeatType", request, 10, 5));
	}

	@Test
	void determineSeatType_ShouldReturnDefault_ForOtherRows() {
		CinemaHallRequest request = CinemaHallRequest.builder().rows(10).defaultSeatType(SeatType.STANDARD).build();

		assertEquals(SeatType.STANDARD,
				ReflectionTestUtils.invokeMethod(cinemaHallService, "determineSeatType", request, 1, 1));
		assertEquals(SeatType.STANDARD,
				ReflectionTestUtils.invokeMethod(cinemaHallService, "determineSeatType", request, 8, 3));
	}

	@Test
	void determineSeatType_ShouldReturnDefault_WhenVIPTypes() {
		CinemaHallRequest request = CinemaHallRequest.builder().rows(10).defaultSeatType(SeatType.STANDARD).build();

		assertEquals(SeatType.STANDARD,
				ReflectionTestUtils.invokeMethod(cinemaHallService, "determineSeatType", request, 1, 1));
		assertEquals(SeatType.VIP,
				ReflectionTestUtils.invokeMethod(cinemaHallService, "determineSeatType", request, 10, 1));
	}
}