package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
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
import ua.lviv.bas.cinema.dao.SeatRepository;
import ua.lviv.bas.cinema.dao.TicketRepository;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.SeatCreateDto;
import ua.lviv.bas.cinema.dto.SeatDto;
import ua.lviv.bas.cinema.mapper.SeatMapper;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private SeatMapper seatMapper;

	@InjectMocks
	private SeatService seatService;

	private SeatCreateDto seatCreateDto;
	private Seat seat;
	private SeatDto seatDto;

	@BeforeEach
	void setUp() {
		seatCreateDto = SeatCreateDto.builder().row(1).number(5).seatType(SeatType.VIP).build();

		seat = Seat.builder().id(1L).row(1).number(5).seatType(SeatType.VIP).build();

		seatDto = SeatDto.builder().id(1L).row(1).number(5).seatType(SeatType.VIP).available(true).build();
	}

	@Test
	void getSeatsByHallId_ShouldReturnSeats() {
		List<Seat> seats = Arrays.asList(seat);

		when(seatRepository.findByHallId(1L)).thenReturn(seats);
		when(seatMapper.toDto(seat)).thenReturn(seatDto);

		List<SeatDto> result = seatService.getSeatsByHallId(1L);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(seatRepository).findByHallId(1L);
	}

	@Test
	void createSeat_ShouldCreateAndReturnSeat() {
		when(seatMapper.toEntity(seatCreateDto)).thenReturn(seat);
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toDto(seat)).thenReturn(seatDto);

		SeatDto result = seatService.createSeat(seatCreateDto);

		assertNotNull(result);
		assertEquals(seatDto, result);
		verify(seatRepository).save(seat);
	}

	@Test
	void getSeatById_WhenExists_ShouldReturnSeat() {
		when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
		when(seatMapper.toDto(seat)).thenReturn(seatDto);

		SeatDto result = seatService.getSeatById(1L);

		assertNotNull(result);
		assertEquals(seatDto, result);
		verify(seatRepository).findById(1L);
	}

	@Test
	void getSeatById_WhenNotExists_ShouldThrowException() {
		when(seatRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			seatService.getSeatById(1L);
		});
		verify(seatRepository).findById(1L);
	}

	@Test
	void updateSeat_WhenExists_ShouldUpdateAndReturnSeat() {
		SeatCreateDto updateDto = SeatCreateDto.builder().row(2).number(10).seatType(SeatType.STANDARD).build();

		Seat updatedSeat = Seat.builder().id(1L).row(2).number(10).seatType(SeatType.STANDARD).build();

		SeatDto updatedDto = SeatDto.builder().id(1L).row(2).number(10).seatType(SeatType.STANDARD).build();

		when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(updatedSeat);
		when(seatMapper.toDto(updatedSeat)).thenReturn(updatedDto);

		SeatDto result = seatService.updateSeat(1L, updateDto);

		assertNotNull(result);
		assertEquals(2, result.getRow());
		assertEquals(10, result.getNumber());
		assertEquals(SeatType.STANDARD, result.getSeatType());
		verify(seatRepository).findById(1L);
		verify(seatRepository).save(seat);
	}

	@Test
	void updateSeat_WhenNotExists_ShouldThrowException() {
		when(seatRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> {
			seatService.updateSeat(1L, seatCreateDto);
		});
		verify(seatRepository).findById(1L);
	}

	@Test
	void deleteSeat_WhenExists_ShouldDeleteSeat() {
		when(seatRepository.existsById(1L)).thenReturn(true);
		doNothing().when(seatRepository).deleteById(1L);

		seatService.deleteSeat(1L);

		verify(seatRepository).existsById(1L);
		verify(seatRepository).deleteById(1L);
	}

	@Test
	void deleteSeat_WhenNotExists_ShouldThrowException() {
		when(seatRepository.existsById(1L)).thenReturn(false);

		assertThrows(EntityNotFoundException.class, () -> {
			seatService.deleteSeat(1L);
		});
		verify(seatRepository).existsById(1L);
	}

	@Test
	void getAvailableSeatsForSession_ShouldReturnAvailableSeats() {
		List<Seat> allSeats = Arrays.asList(seat);
		when(seatRepository.findByHallId(1L)).thenReturn(allSeats);
		when(ticketRepository.existsBySeatIdAndSessionId(1L, 1L)).thenReturn(false);
		when(seatMapper.toDto(seat)).thenReturn(seatDto);

		List<SeatDto> result = seatService.getAvailableSeatsForSession(1L, 1L);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.get(0).isAvailable());
		verify(seatRepository).findByHallId(1L);
		verify(ticketRepository).existsBySeatIdAndSessionId(1L, 1L);
	}

	@Test
	void getAllSeatsForSession_ShouldReturnAllSeatsWithAvailability() {
		List<Seat> allSeats = Arrays.asList(seat);
		when(seatRepository.findByHallId(1L)).thenReturn(allSeats);
		when(ticketRepository.existsBySeatIdAndSessionId(1L, 1L)).thenReturn(true);
		when(seatMapper.toDto(seat)).thenReturn(seatDto);

		List<SeatDto> result = seatService.getAllSeatsForSession(1L, 1L);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertFalse(result.get(0).isAvailable());
		verify(seatRepository).findByHallId(1L);
		verify(ticketRepository).existsBySeatIdAndSessionId(1L, 1L);
	}

	@Test
	void getSeatEntityById_WhenExists_ShouldReturnEntity() {
		when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

		Seat result = seatService.getSeatEntityById(1L);

		assertNotNull(result);
		assertEquals(seat, result);
		verify(seatRepository).findById(1L);
	}

	@Test
	void existsById_ShouldReturnTrueWhenExists() {
		when(seatRepository.existsById(1L)).thenReturn(true);

		boolean result = seatService.existsById(1L);

		assertTrue(result);
		verify(seatRepository).existsById(1L);
	}

	@Test
	void isSeatAvailable_WhenAvailable_ShouldReturnTrue() {
		when(ticketRepository.existsBySeatIdAndSessionId(1L, 1L)).thenReturn(false);

		boolean result = seatService.isSeatAvailable(1L, 1L);

		assertTrue(result);
		verify(ticketRepository).existsBySeatIdAndSessionId(1L, 1L);
	}

	@Test
	void getAvailableSeatsCountForSession_ShouldReturnCorrectCount() {
		when(seatRepository.countByHallId(1L)).thenReturn(50L);
		when(ticketRepository.countBySessionId(1L)).thenReturn((int) 20L);

		int result = seatService.getAvailableSeatsCountForSession(1L, 1L);

		assertEquals(30, result);
		verify(seatRepository).countByHallId(1L);
		verify(ticketRepository).countBySessionId(1L);
	}
}