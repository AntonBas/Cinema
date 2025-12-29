package ua.lviv.bas.cinema.service.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.SeatRepository;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private SeatMapper seatMapper;

	@InjectMocks
	private SeatService seatService;

	private CinemaHall testHall;
	private Seat standardSeat;
	private Seat vipSeat;
	private Seat inactiveSeat;
	private SeatResponse standardSeatDto;
	private SeatResponse vipSeatDto;
	private SeatResponse inactiveSeatDto;

	@BeforeEach
	void setUp() {
		testHall = new CinemaHall();
		testHall.setId(1L);
		testHall.setName("Hall A");

		standardSeat = Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).hall(testHall).active(true)
				.build();

		vipSeat = Seat.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).hall(testHall).active(true).build();

		inactiveSeat = Seat.builder().id(3L).row(2).number(1).seatType(SeatType.STANDARD).hall(testHall).active(false)
				.build();

		standardSeatDto = SeatResponse.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).active(true)
				.build();

		vipSeatDto = SeatResponse.builder().id(2L).row(1).number(2).seatType(SeatType.VIP).active(true).build();

		inactiveSeatDto = SeatResponse.builder().id(3L).row(2).number(1).seatType(SeatType.STANDARD).active(false)
				.build();
	}

	@Test
	void getSeatById_ShouldReturnSeatDto_WhenSeatExists() {
		when(seatRepository.findById(1L)).thenReturn(Optional.of(standardSeat));
		when(seatMapper.toDto(standardSeat)).thenReturn(standardSeatDto);

		SeatResponse result = seatService.getSeatById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertTrue(result.isActive());
		verify(seatRepository).findById(1L);
		verify(seatMapper).toDto(standardSeat);
	}

	@Test
	void getSeatById_ShouldThrowSeatNotFoundException_WhenSeatNotExists() {
		when(seatRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(SeatNotFoundException.class, () -> seatService.getSeatById(1L));
		verify(seatRepository).findById(1L);
		verify(seatMapper, never()).toDto(any());
	}

	@Test
	void updateSeatType_ShouldUpdateAndReturnSeatDto_WhenSeatExists() {
		when(seatRepository.findById(1L)).thenReturn(Optional.of(standardSeat));
		when(seatRepository.save(standardSeat)).thenReturn(vipSeat);
		when(seatMapper.toDto(vipSeat)).thenReturn(vipSeatDto);

		SeatResponse result = seatService.updateSeatType(1L, SeatType.VIP);

		assertNotNull(result);
		assertEquals(SeatType.VIP, result.getSeatType());
		assertEquals(SeatType.VIP, standardSeat.getSeatType());
		verify(seatRepository).findById(1L);
		verify(seatRepository).save(standardSeat);
		verify(seatMapper).toDto(vipSeat);
	}

	@Test
	void updateSeatType_ShouldThrowSeatNotFoundException_WhenSeatNotExists() {
		when(seatRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(SeatNotFoundException.class, () -> seatService.updateSeatType(1L, SeatType.VIP));
		verify(seatRepository).findById(1L);
		verify(seatRepository, never()).save(any());
	}

	@Test
	void getSeatsByHall_ShouldReturnListOfSeatDtos() {
		List<Seat> seats = Arrays.asList(standardSeat, vipSeat, inactiveSeat);
		List<SeatResponse> seatDtos = Arrays.asList(standardSeatDto, vipSeatDto, inactiveSeatDto);

		when(seatRepository.findByHallId(1L)).thenReturn(seats);
		when(seatMapper.toDtoList(seats)).thenReturn(seatDtos);

		List<SeatResponse> result = seatService.getSeatsByHall(1L);

		assertNotNull(result);
		assertEquals(3, result.size());
		verify(seatRepository).findByHallId(1L);
		verify(seatMapper).toDtoList(seats);
	}

	@Test
	void getSeatByPosition_ShouldReturnSeatDto_WhenSeatExists() {
		when(seatRepository.findByHallIdAndRowAndNumber(1L, 1, 1)).thenReturn(Optional.of(standardSeat));
		when(seatMapper.toDto(standardSeat)).thenReturn(standardSeatDto);

		SeatResponse result = seatService.getSeatByPosition(1L, 1, 1);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		verify(seatRepository).findByHallIdAndRowAndNumber(1L, 1, 1);
		verify(seatMapper).toDto(standardSeat);
	}

	@Test
	void getSeatByPosition_ShouldThrowSeatNotFoundException_WhenSeatNotExists() {
		when(seatRepository.findByHallIdAndRowAndNumber(1L, 1, 1)).thenReturn(Optional.empty());

		assertThrows(SeatNotFoundException.class, () -> seatService.getSeatByPosition(1L, 1, 1));
		verify(seatRepository).findByHallIdAndRowAndNumber(1L, 1, 1);
		verify(seatMapper, never()).toDto(any());
	}

	@Test
	void isSeatAvailable_ShouldReturnTrue_WhenSeatActiveAndExists() {
		when(seatRepository.existsByHallIdAndRowAndNumberAndActiveTrue(1L, 1, 1)).thenReturn(true);

		boolean result = seatService.isSeatAvailable(1L, 1, 1);

		assertTrue(result);
		verify(seatRepository).existsByHallIdAndRowAndNumberAndActiveTrue(1L, 1, 1);
	}

	@Test
	void isSeatAvailable_ShouldReturnFalse_WhenSeatNotActiveOrNotExists() {
		when(seatRepository.existsByHallIdAndRowAndNumberAndActiveTrue(1L, 2, 1)).thenReturn(false);

		boolean result = seatService.isSeatAvailable(1L, 2, 1);

		assertFalse(result);
		verify(seatRepository).existsByHallIdAndRowAndNumberAndActiveTrue(1L, 2, 1);
	}

	@Test
	void countSeatsByHall_ShouldReturnCorrectCount() {
		when(seatRepository.countByHallId(1L)).thenReturn(5L);

		long result = seatService.countSeatsByHall(1L);

		assertEquals(5L, result);
		verify(seatRepository).countByHallId(1L);
	}

	@Test
	void getSeatsByType_ShouldReturnFilteredSeatDtos() {
		List<Seat> vipSeats = Arrays.asList(vipSeat);
		List<SeatResponse> vipSeatDtos = Arrays.asList(vipSeatDto);

		when(seatRepository.findByHallIdAndSeatType(1L, SeatType.VIP)).thenReturn(vipSeats);
		when(seatMapper.toDtoList(vipSeats)).thenReturn(vipSeatDtos);

		List<SeatResponse> result = seatService.getSeatsByType(1L, SeatType.VIP);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(SeatType.VIP, result.get(0).getSeatType());
		verify(seatRepository).findByHallIdAndSeatType(1L, SeatType.VIP);
		verify(seatMapper).toDtoList(vipSeats);
	}

	@Test
	void activateSeat_ShouldActivateSeatAndReturnDto_WhenSeatExists() {
		when(seatRepository.findById(3L)).thenReturn(Optional.of(inactiveSeat));
		when(seatRepository.save(inactiveSeat)).thenReturn(inactiveSeat);
		when(seatMapper.toDto(inactiveSeat)).thenReturn(inactiveSeatDto);

		SeatResponse result = seatService.activateSeat(3L);

		assertNotNull(result);
		assertTrue(inactiveSeat.isActive());
		verify(seatRepository).findById(3L);
		verify(seatRepository).save(inactiveSeat);
		verify(seatMapper).toDto(inactiveSeat);
	}

	@Test
	void activateSeat_ShouldThrowSeatNotFoundException_WhenSeatNotExists() {
		when(seatRepository.findById(3L)).thenReturn(Optional.empty());

		assertThrows(SeatNotFoundException.class, () -> seatService.activateSeat(3L));
		verify(seatRepository).findById(3L);
		verify(seatRepository, never()).save(any());
	}

	@Test
	void deactivateSeat_ShouldDeactivateSeatAndReturnDto_WhenSeatExists() {
		when(seatRepository.findById(1L)).thenReturn(Optional.of(standardSeat));
		when(seatRepository.save(standardSeat)).thenReturn(standardSeat);
		when(seatMapper.toDto(standardSeat)).thenReturn(standardSeatDto);

		SeatResponse result = seatService.deactivateSeat(1L);

		assertNotNull(result);
		assertFalse(standardSeat.isActive());
		verify(seatRepository).findById(1L);
		verify(seatRepository).save(standardSeat);
		verify(seatMapper).toDto(standardSeat);
	}

	@Test
	void deactivateSeat_ShouldThrowSeatNotFoundException_WhenSeatNotExists() {
		when(seatRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(SeatNotFoundException.class, () -> seatService.deactivateSeat(1L));
		verify(seatRepository).findById(1L);
		verify(seatRepository, never()).save(any());
	}

	@Test
	void getActiveSeatsByHall_ShouldReturnOnlyActiveSeats() {
		List<Seat> activeSeats = Arrays.asList(standardSeat, vipSeat);
		List<SeatResponse> activeSeatDtos = Arrays.asList(standardSeatDto, vipSeatDto);

		when(seatRepository.findByHallIdAndActiveTrue(1L)).thenReturn(activeSeats);
		when(seatMapper.toDtoList(activeSeats)).thenReturn(activeSeatDtos);

		List<SeatResponse> result = seatService.getActiveSeatsByHall(1L);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.get(0).isActive());
		assertTrue(result.get(1).isActive());
		verify(seatRepository).findByHallIdAndActiveTrue(1L);
		verify(seatMapper).toDtoList(activeSeats);
	}
}